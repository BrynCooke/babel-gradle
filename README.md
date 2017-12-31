Quick and dirty babel plugin for gradle.

Add the plugin and specify the presets that you want to use. For example:

```
plugins {
    id 'com.github.bryncooke.babel-gradle' version '1.0.1'
}


babel {
    presets "react", "es2015"
}

```

Place your artifacts in `src/main/babel` for processing.

If the processResources task is present then it will automatically depend on the babel task

If sourceSets are available then the output directory will automatically be added to the main resources. 
