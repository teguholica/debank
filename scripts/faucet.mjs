import { Keypair, TransactionBuilder, Networks, Operation, Asset, BASE_FEE, Horizon } from '@stellar/stellar-sdk';

const HORIZON = 'https://horizon-testnet.stellar.org';
const FRIENDBOT = 'https://friendbot.stellar.org';
const server = new Horizon.Server(HORIZON);

async function fund(addr, retries = 3) {
  for (let i = 0; i < retries; i++) {
    const res = await fetch(`${FRIENDBOT}?addr=${addr}`);
    if (res.ok) return res.json();
    const body = await res.text();
    if (res.status === 400 && body.includes('already exists')) return;
    if (res.status === 500 && i < retries - 1) {
      console.log(`  friendbot 500, retrying (${i + 1}/${retries - 1})...`);
      await new Promise(r => setTimeout(r, 2000));
      continue;
    }
    throw new Error(`friendbot failed (HTTP ${res.status}): ${body}`);
  }
}

async function setup() {
  const issuer = Keypair.random();
  console.log('Funding issuer...');
  await fund(issuer.publicKey());
  console.log('Issuer funded!\n');
  console.log('=== Copy these to StellarConfig.kt ===');
  console.log(`const val IDR_ISSUER_PUBLIC_KEY = "${issuer.publicKey()}"`);
  console.log(`const val IDR_ISSUER_SECRET_SEED = "${issuer.secret()}"`);
  console.log('======================================\n');

  // Save issuer keypair to JSON for reuse
  const fs = await import('fs/promises');
  await fs.writeFile('.issuer.json', JSON.stringify({
    publicKey: issuer.publicKey(),
    secretSeed: issuer.secret(),
  }, null, 2));
  console.log('Issuer key saved to .issuer.json (reuse with: node faucet.mjs fund <addr>)\n');

  return issuer;
}

async function fundIdr(recipientAddr, amount) {
  let issuer;
  try {
    const fs = await import('fs/promises');
    const saved = JSON.parse(await fs.readFile('.issuer.json', 'utf-8'));
    issuer = Keypair.fromSecret(saved.secretSeed);
    console.log(`Using issuer: ${issuer.publicKey()}`);
  } catch {
    console.log('No saved issuer found, creating new one...');
    issuer = await setup();
  }

  // Ensure issuer is funded
  console.log('Ensuring issuer is funded...');
  try { await fund(issuer.publicKey()); } catch {}

  // Ensure recipient is funded
  console.log('Ensuring recipient is funded...');
  try { await fund(recipientAddr); } catch {}

  // Load recipient account to check if trustline exists
  const recipientAcc = await server.loadAccount(recipientAddr);
  const asset = new Asset(process.env.ASSET_CODE || 'IDR', issuer.publicKey());
  const hasTrustline = recipientAcc.balances.some(b =>
    b.asset_type !== 'native' && b.asset_code === asset.code && b.asset_issuer === asset.issuer
  );

  if (!hasTrustline) {
    throw new Error(
      `Recipient ${recipientAddr} has no trustline for ${asset.code}:${asset.issuer}.\n` +
      `Open the app, go to Send flow first (this creates the trustline), then retry.`
    );
  }

  // Send IDR from issuer to recipient
  const issuerAcc = await server.loadAccount(issuer.publicKey());
  const tx = new TransactionBuilder(issuerAcc, {
    fee: BASE_FEE,
    networkPassphrase: Networks.TESTNET,
  })
    .addOperation(Operation.payment({
      destination: recipientAddr,
      asset,
      amount: amount.toString(),
    }))
    .setTimeout(30)
    .build();

  tx.sign(issuer);
  const result = await server.submitTransaction(tx);
  console.log(`Sent ${amount} ${asset.code} to ${recipientAddr}`);
  console.log(`Tx hash: ${result.hash}`);
}

async function main() {
  const cmd = process.argv[2];

  if (cmd === 'setup') {
    await setup();
  } else if (cmd === 'fund') {
    const addr = process.argv[3];
    if (!addr) {
      console.error('Usage: node faucet.mjs fund <recipient-address> [amount]');
      process.exit(1);
    }
    const amount = process.argv[4] || '1000.0000000';
    await fundIdr(addr, amount);
  } else {
    console.log('Usage:');
    console.log('  node faucet.mjs setup              # Create issuer + print config');
    console.log('  node faucet.mjs fund <addr> [amt]  # Send IDR to recipient');
    process.exit(1);
  }
}

main().catch(e => {
  console.error(e.message);
  process.exit(1);
});
