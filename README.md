[![Build Status](https://travis-ci.org/radicalbit/sbt-dependency-from-file.svg?branch=master)](https://travis-ci.org/radicalbit/sbt-dependency-from-file)

# sbt-dependency-from-file
`sbt-dependency-from-file` is a fresh-made library for dynamic load sbt dependencies using json file.

## Usage

In order to use `sbt-dependency-from-file` in  sbt project, add sbt plugin in `project/plugins.sbt`: 
    
    resolvers += Resolver.url("radicalbit-plugins", url("https://dl.bintray.com/radicalbit/sbt-plugins"))(Resolver.ivyStylePatterns)
    addSbtPlugin("io.radicalbit" % "sbt-dependency-from-file" % "1.0")
    
## Example

Suppose that you want to load different sbt dependencies according to specific environment, you just need to define a json file that contains dependecies in the root folder of your project:

```json [
          {
            "groupId":"org.typelevel",
            "artifactId":"cats-core",
            "version":"2.0.0-RC1",
            "scalaVersion": "2.12",
            "resolver":{
              "name":"Local Maven Repository",
              "url":".m2/repository",
            }
          }
        ]
```
And then, in your `build.sbt`:
    
```scala 
    dependenciesJsonPath := baseDirectory.value / "dev.json"
    libraryDependencies ++= extractedDependencies.value
```    
    
## Authors
* **Francesco Frontera** - [francesco.frontera@radicalbit.io](mailto:francesco.frontera@radicalbit.io) [@francescofrontera](https://github.com/francescofrontera)
* **Mauro Cortellazzi** - [mauro.cortellazzi@radicalbit.io](mailto:mauro.cortellazzi@radicalbit.io) [@maocorte](https://github.com/maocorte)    
