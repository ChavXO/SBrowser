import cbt._
class Build(context: cbt.Context) extends PackageBuild(context){
  override def version = "0.0.1"
  override def groupId = "org.bensemic"
  override def artifactId = "sbrowser"
  override def dependencies = super.dependencies ++ Vector(
    ScalaDependency("org.scalafx", "scalafx", "8.0.60-R9")
  )
  override def compile = {
    println("Compiling...")
    super.compile
  }
}