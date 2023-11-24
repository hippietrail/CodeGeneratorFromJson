# CodeGeneratorFromJson
Example of how to add generated code to an Android app Gradle build system

In this particular example it downloads JSON which is exported as a `String` in the generated code.

If the generated code file already exists it uses a shorter HTTP timeout, to keep the build fast even when the JSON host is slow.

This makes sense when you want to embed JSON that's updated over time and it's acceptable to have a reasonably up-to-date version and not always the absolute latest.

That's not the only logic which could make sense for any application of course.
