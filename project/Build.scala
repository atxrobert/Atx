import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "atx"
    val appVersion      = "1.0"

    unmanagedBase <<= baseDirectory { base => base / "lib" }
    
    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val main = PlayProject(appName, appVersion, appDependencies).settings(defaultJavaSettings:_*).settings(
      // Add your own project settings here      
    )

}
