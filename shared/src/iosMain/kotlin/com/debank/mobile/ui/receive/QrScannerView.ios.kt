package com.debank.mobile.ui.receive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerView(
    onResult: (String) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier
) {
    val session = remember { AVCaptureSession() }

    LaunchedEffect(Unit) {
        AVCaptureDevice.requestAccessForMediaType("vide") { granted ->
            if (!granted) {
                onError(IllegalStateException("Camera permission denied"))
            }
        }
    }

    DisposableEffect(Unit) {
        session.startRunning()
        onDispose { session.stopRunning() }
    }

    UIKitView(
        factory = {
            val captureDevice = AVCaptureDevice.defaultDeviceWithMediaType("vide")
            if (captureDevice == null) {
                onError(IllegalStateException("No camera available"))
                return@UIKitView UIView()
            }

            val input = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, null)
            if (input == null) {
                onError(IllegalStateException("Cannot create camera input"))
                return@UIKitView UIView()
            }
            session.addInput(input as AVCaptureInput)

            val metadataOutput = AVCaptureMetadataOutput()
            session.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(
                object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
                    override fun captureOutput(
                        output: AVCaptureOutput,
                        didOutputMetadataObjects: List<*>,
                        fromConnection: AVCaptureConnection
                    ) {
                        didOutputMetadataObjects.forEach { obj ->
                            val code = obj as? AVMetadataMachineReadableCodeObject
                            val str = code?.stringValue ?: return@forEach
                            if (str.isNotBlank()) {
                                onResult(str)
                            }
                        }
                    }
                },
                null
            )
            @Suppress("UNCHECKED_CAST")
            metadataOutput.metadataObjectTypes = listOf("org.iso.QRCode" as Any)

            val view = UIView()
            val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(session)
            previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            previewLayer.frame = view.bounds
            view.layer.addSublayer(previewLayer)

            view
        },
        modifier = modifier,
        update = { view ->
            val previewLayer = view.layer.sublayers?.firstOrNull() as? AVCaptureVideoPreviewLayer
            previewLayer?.frame = view.bounds
        }
    )
}
