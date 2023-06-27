package com.deny.calculatorimage.viewmodel

import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {

    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _resultEvaluateLiveData: MutableLiveData<Double> = MutableLiveData()
    val resultEvaluateLiveData: LiveData<Double> = _resultEvaluateLiveData

    private val _resultrecognizeTextLiveData: MutableLiveData<String> = MutableLiveData()
    val resultrecognizeTextLiveData: LiveData<String> = _resultrecognizeTextLiveData

    val imageResultUri: MutableLiveData<Uri> = MutableLiveData()

    fun processImage(image: InputImage) {
        viewModelScope.launch {
            try {
                val visionText = recognizeText(image)
                val result = extractExpression(visionText)
                if (result != null) {
                    val expression = result.first
                    val evaluationResult = evaluateExpression(expression)
                    _resultrecognizeTextLiveData.postValue(expression)
                    _resultEvaluateLiveData.postValue(evaluationResult)
                } else {
                    _resultrecognizeTextLiveData.postValue(null)
                    _resultEvaluateLiveData.postValue(null)
                }
            } catch (e: MlKitException) {
                Log.e("CalculatorViewModel", "Text recognition error: ${e.localizedMessage}")
                _resultrecognizeTextLiveData.postValue(null)
                _resultEvaluateLiveData.postValue(null)
            }
        }
    }

    private suspend fun recognizeText(image: InputImage): Text {
        return withContext(Dispatchers.Default) {
            Tasks.await(recognizer.process(image))
        }
    }

    private fun extractExpression(visionText: Text): Pair<String, Rect>? = runBlocking {
        val blocks = visionText.textBlocks
        for (block in blocks) {
            val lines = block.lines
            for (line in lines) {
                val elements = line.elements
                val expressionBuilder = StringBuilder()
                for (element in elements) {
                    val elementText = element.text
                    // Ignore empty spaces
                    if (elementText.isNotBlank() && elementText.matches(Regex("[0-9+\\-*/x:]+"))) {
                        val modifiedText = async(Dispatchers.Default) {
                            elementText.replace("x", "*").replace(":", "/")
                        }
                        expressionBuilder.append(modifiedText.await())
                    }
                }
                val expression = expressionBuilder.toString()
                if (isExpression(expression)) {
                    return@runBlocking Pair(expression, line.boundingBox!!)
                }
            }
        }
        return@runBlocking null
    }
    private fun isExpression(text: String): Boolean = runBlocking {
        val modifiedText = async(Dispatchers.Default) {
            text.replace("x", "*").replace(":", "/")
        }
        val isMatching = async(Dispatchers.Default) {
            modifiedText.await().matches(Regex("^\\d+[-+*/]\\d+$"))
        }
        isMatching.await()
    }

    private fun evaluateExpression(expression: String): Double = runBlocking {
        val parts = expression.split(Regex("[-+*/]"))
        val operator = expression[parts[0].length].toString()
        val num1 = parts[0].toDouble()
        val num2 = parts[1].toDouble()

        return@runBlocking when (operator) {
            "+" -> {
                val result = async(Dispatchers.Default) { num1 + num2 }
                result.await()
            }
            "-" -> {
                val result = async(Dispatchers.Default) { num1 - num2 }
                result.await()
            }
            "*" -> {
                val result = async(Dispatchers.Default) { num1 * num2 }
                result.await()
            }
            "/" -> {
                val result = async(Dispatchers.Default) { num1 / num2 }
                result.await()
            }
            else -> throw IllegalArgumentException("Invalid operator")
        }
    }
}
