package com.example.codegeneratorfromjson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.codegeneratorfromjson.ui.theme.CodeGeneratorFromJsonTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ParsedFromJson(
    val name: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeGeneratorFromJsonTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val jsonString = jsonFromGeneratedCode
                    val jsonObject = Json.decodeFromString<ParsedFromJson>(jsonString)

                    Greeting(jsonObject.name)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodeGeneratorFromJsonTheme {
        Greeting("Android")
    }
}