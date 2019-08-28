# sbt-dependency-from-file
`sbt-dependency-from-file` is a fresh-made library for dynamic load sbt dependecies using json file.

## Usage

In order to use `sbt-dependency-from-file` in  sbt project, include plugin in `project/plugins.sbt` file: 

    addSbtPlugin("io.radicalbit" % "sbt-dependency-from-file" % "0.1")
    
## Example
* Suppose that you want to load different sbt dependencies according to specific environment, you just need to define a json file that contains dependecies in the root folder of project:


        [
          {
            "groupId":""org.typelevel"",
            "artifactId":"cats-core",
            "version":"2.0.0-RC1",
            "scalaVersion": "2.12",
            "resolver":{
              "name":"Local Maven Repository",
              "url":".m2/repository",
            }
          }
        ]

And then, in your `build.sbt`:
    
    dependenciesJsonPath := baseDirectory.value / "dev.json"
    libraryDependencies ++= extractedDependencies.value
    
    
         


            

    