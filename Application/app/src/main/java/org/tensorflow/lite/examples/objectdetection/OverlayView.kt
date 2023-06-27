/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt


class OverlayView(context: Context?, attrs: AttributeSet? ) : View(context, attrs) {

    private var results: List<Detection> = LinkedList<Detection>()
    private var WarningThreshold  : Float = 2.5F
    private var StopThreshold  : Float = 5.0F
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var limitingBoxes = Paint()
    private lateinit var mediaPlayer: MediaPlayer





    private var scaleFactor: Float = 1f





    //    var currentId = 0
    private var lastWarningMillis = SystemClock.uptimeMillis()
    private var lastStopMillis = SystemClock.uptimeMillis()
    private var lastWarningObject = ""

//
//    class Part(var horizontalcentroid: Float, var verticalcentroid: Float, var TTL: Float)


    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE


        limitingBoxes.color = Color.BLACK
        limitingBoxes.strokeWidth = 15F
        limitingBoxes.style = Paint.Style.STROKE
    }


//    private fun track() {
//        val trackDetails = mutableMapOf<Int, Part>()
//        for (result in results) {
//            println("Tracking.....")
//            val boundingBox = result.boundingBox
//            var horizontalCenter = (boundingBox.left + boundingBox.right) / 2
//            var verticalCenter = (boundingBox.bottom + boundingBox.top) / 2
//            val tempPart = Part(horizontalCenter, verticalCenter, 1.0F)
//            if (trackDetails.isNotEmpty()) {
//                for(trackDetail in trackDetails){
//                    println("Here")
//                    trackDetails[currentId] = tempPart
//                    currentId += 1
//                    println(tempPart.horizontalcentroid)
//                    println("data : ${trackDetails[currentId]?.TTL}")
//                    println("current id $currentId")
//                }
//
//            } else {
//                trackDetails[currentId] = tempPart
//                currentId += 1
//                println("current id $currentId")
//            }
//        }
//    }

    override fun draw(canvas: Canvas) {

        super.draw(canvas)

        var minDistance = 10
        var minObject = ""
        var rightFlag = 0
        var leftFlag = 0
//        track()

        for (result in results) {
            val boundingBox = result.boundingBox

            val imageW = 1080 //Example
            val imageWQ = imageW / 6
//            var verticalCenter = (boundingBox.bottom + boundingBox.top) / 2
            val startBoundaries = imageWQ
            val endBoundaries = imageW - imageWQ

//            println("R : ${boundingBox.right}")
//            println("L  : ${boundingBox.left}")
//            var centroid = floatArrayOf(verticalCenter, horizontalCenter)
//            println("Left Bounds: $ImageWQ")
//            println("Right Bound: $endboundries")
//            println("Center : $horizontalCenter")


            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            val horizontalCenter = (left + right) / 2



            val drawableRect2 = RectF(0.0F, 0.0F, startBoundaries.toFloat(), 2000.0F)
            canvas.drawRect(drawableRect2, limitingBoxes)
            val drawableRect3 = RectF(endBoundaries.toFloat(), 0.0F, 1080.0F, 2000.0F)
            canvas.drawRect(drawableRect3, limitingBoxes)

            val objectHeight = bottom - top
            var currentDistance = 100.00

            currentDistance = if (result.categories[0].label == "person") {
                7.573197378004368 + (-0.005651857246904584 * objectHeight)
            } else if ((result.categories[0].label) == "car") {
                7.802089824090496 + (-0.006659885922446433 * objectHeight)
            } else if ((result.categories[0].label) == "bus") {
                7.3020750539 + (-0.0026328488702168714 * objectHeight)
            } else {
                4.8
            }

            if ((startBoundaries < horizontalCenter) && (horizontalCenter < endBoundaries)) {

                if (currentDistance < 7) {
                    if (currentDistance < minDistance) {
                        minDistance = currentDistance.roundToInt()
                        minObject = result.categories[0].label
                        if (minObject == "stop sign") {
                            minObject = "stopsign"
                        }
                    }

                    // Draw bounding box around detected objects
                    val drawableRect = RectF(left, top, right, bottom)
                    canvas.drawRect(drawableRect, boxPaint)



                    if (currentDistance < 0) {
                        currentDistance = 0.0
                    }

                    val drawableText =
                        result.categories[0].label + " " + (currentDistance.toInt()).toString()

                    textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
                    val textWidth = bounds.width()
                    val textHeight = bounds.height()
                    canvas.drawRect(
                        left,
                        top,
                        left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                        top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                        textBackgroundPaint
                    )

                    // Draw text for detected object
                    canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
                }
            }

//            println("Start Boundries = $startBoundaries  .......... Left = $left ........ object = ${result.categories[0].label}")
            if ((left < startBoundaries) && currentDistance < 2) {
//                println("Left Not Clear")
                leftFlag = 1
            }
//            println("End Boundries = $endBoundaries  .......... Right = $right ........ object = ${result.categories[0].label}")
            if ((right > endBoundaries) && currentDistance < 2) {
//                println("Right Not Clear")
                rightFlag = 1
            }

        }

        if (minDistance < StopThreshold) {
            if (((SystemClock.uptimeMillis()) - lastStopMillis > 2500) || ((lastWarningObject != minObject) && ((SystemClock.uptimeMillis()) - lastStopMillis > 1000))) {
                lastStopMillis = SystemClock.uptimeMillis()
                lastWarningObject = minObject
                if (rightFlag == 0) {
                    val audioID2 = resources.getIdentifier(
                        "stoprightclear",
                        "raw",
                        "org.tensorflow.lite.examples.objectdetection"
                    )
                    mediaPlayer = MediaPlayer.create(context, audioID2)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    }
                } else if (leftFlag == 0) {
                    val audioID2 = resources.getIdentifier(
                        "stopleftclear",
                        "raw",
                        "org.tensorflow.lite.examples.objectdetection"
                    )
                    mediaPlayer = MediaPlayer.create(context, audioID2)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    }
                } else {
                    val audioID = resources.getIdentifier(
                        "stop",
                        "raw",
                        "org.tensorflow.lite.examples.objectdetection"
                    )
                    mediaPlayer = MediaPlayer.create(context, audioID)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    }
                }
            }
        } else if (minDistance <= WarningThreshold) {
            println("minObject  $minObject")
//            println("Current Millis : ${SystemClock.uptimeMillis()}")
//            println("Last Warning Time : $lastWarningMillis")
            if ((((SystemClock.uptimeMillis()) - lastWarningMillis > 3000) && (((SystemClock.uptimeMillis()) - lastStopMillis > 3000)) || (lastWarningObject != minObject)) && ((SystemClock.uptimeMillis()) - lastWarningMillis > 1000)) {
                lastWarningMillis = SystemClock.uptimeMillis()
                lastWarningObject = minObject
                val audioID = resources.getIdentifier(
                    minObject,
                    "raw",
                    "org.tensorflow.lite.examples.objectdetection"
                )
                mediaPlayer = MediaPlayer.create(context, audioID)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
            }
        }
//        println("Outside")
    }

    fun setResults(
        detectionResults: MutableList<Detection>,

        imageHeight: Int,
        imageWidth: Int,
    ) {
        results = detectionResults

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }
    fun setThreshold(
        threshold :  Int
    ){
        if (threshold == 1 ){
            StopThreshold = 1.5F
            WarningThreshold  =3.0F
        }else{
            StopThreshold = 2.5F
            WarningThreshold  =5.0F
        }
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
