[![Build Status](https://travis-ci.org/radicalbit/sbt-dependency-from-file.svg?branch=master)](https://travis-ci.org/radicalbit/sbt-dependency-from-file)

# sbt-dependency-from-file
`sbt-dependency-from-file` is a fresh-made library for dynamic load sbt dependencies using json files.

## Advantages

By using `sbt-dependency-from-file`, users will be able to dynamically change project dependencies by adding or overwriting a simple file; this comes really handy to automation software.

## Usage

In order to use `sbt-dependency-from-file` in a sbt project, add sbt plugin in `project/plugins.sbt`: 
    
    addSbtPlugin("io.radicalbit" % "sbt-dependency-from-file" % "1.5-SNAPSHOT")

And enable the plugin:
```
lazy val core = project
.in(file("modules/core"))
.settings(commonSettings: _*)
.enablePlugins(DependenciesFromJsonPlugin)
```

    
## Example

Suppose that you want to load different sbt dependencies according to *specific environment*, you just need to define a json file that contains dependencies in the root folder of your project:

```json 
[
          {
            "groupId":"org.typelevel",
            "artifactId":"cats-core",
            "version":"2.0.0-RC1",
            "scalaVersion": "2.12",
            "resolver":{
              "name":"Local Maven Repository",
              "url":".m2/repository"
            }
          }
]
```
And then, in your `build.sbt`:
    
```scala 
    dependenciesJsonPath := baseDirectory.value / "dev.json"
    libraryDependencies ++= dependenciesFromJson.value.dependencies
```    
    
## Authors
* **Francesco Frontera** - [francesco.frontera@radicalbit.io](mailto:francesco.frontera@radicalbit.io) [@francescofrontera](https://github.com/francescofrontera)
* **Mauro Cortellazzi** - [mauro.cortellazzi@radicalbit.io](mailto:mauro.cortellazzi@radicalbit.io) [@maocorte](https://github.com/maocorte)    
