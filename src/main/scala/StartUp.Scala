package sbrowser

import java.io._
import java.util.Scanner

package object StartUp {
  val username = System.getProperty("user.name")
  
  val sbFolder = s"/home/${username}/SBrowser/"
  val configFileName = "browser.config"

  case class BrowserConfigs (
    homePage : String,
    javascriptEnable : Boolean
  )

  def loadConfigurations(file : File) : BrowserConfigs = {
    val scan = new Scanner(file)
    val homepage = scan.next()
    val javascriptEnable = if (scan.next() == "true") true else false
    BrowserConfigs(homepage, javascriptEnable)
  }

  val configFile = new File(sbFolder + configFileName)

  def runStartUp() : BrowserConfigs {
    if (configFile.exists) {
      loadConfigurations(configFile)
    } else {
      BrowserConfigs("http://www.google.com", true)
    }
  }
}
