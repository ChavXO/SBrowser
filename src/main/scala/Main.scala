/*
 * Copyright (c) 2011-2016, ScalaFX Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the ScalaFX Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SCALAFX PROJECT OR ITS CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sbrowser

import java.nio._
import java.io._
import java.util.Scanner

import scala.io.Source

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.control.Menu
import scalafx.application.Platform
import scalafx.scene.control.MenuBar
import scalafx.geometry._
import scalafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import scalafx.concurrent.Worker.State
import scalafx.scene.image.Image
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.stage.Stage
import scalafx.scene.control.TextField
import scalafx.scene.control.Button
import scalafx.scene.control.ProgressBar
import scalafx.scene.control.Label
import scalafx.scene.web._
import scalafx.event.ActionEvent
import scalafx.event.EventHandler
import scalafx.scene.input._
import javafx.beans.value.ChangeListener

package object StartUp {
  
  val username = System.getProperty("user.name")
  
  val sbFolder = s"/home/${username}/SBrowser/"
  val configFileName = "browser.config"

  case class BrowserConfigs (
    homepage : String,
    searchEngine : String,
    javascriptEnabled : Boolean
  )

  def loadConfigurations(file : File) : BrowserConfigs = {
    val scan = new Scanner(file)
    val homepage = scan.next()
    val searchEngineUrl = scan.next()
    val javascriptEnable = if (scan.next() == "true") true else false

    BrowserConfigs(homepage, searchEngineUrl, javascriptEnable)
  }

  val configFile = new File(sbFolder + configFileName)

  def runStartUp() : BrowserConfigs = {
    if (System.getProperty("os.name") == "Linux" && configFile.exists) {
      loadConfigurations(configFile)
    } else {
      val sbFolderDir = new File(sbFolder)
      sbFolderDir.mkdir()
      val aConfigFile = new File(sbFolder + configFileName)
      aConfigFile.createNewFile()
      val writer = new PrintWriter(aConfigFile, "UTF-8");
      writer.println("http://www.google.com");
      writer.println("true");
      writer.close();
      BrowserConfigs("http://www.google.com", "http://www.google.com/search?q=", true)
    }
  }
}


object SBrowser extends JFXApp {

  val browserConfigs = StartUp.runStartUp()

  val browser = new WebView {
    hgrow = Priority.Always
    vgrow = Priority.Always
    onStatusChanged = (e: WebEvent[_]) => txfUrl.setText(engine.location.value)

  }

  val buttonStyle = s"""    -fx-background-color: 
                                #c3c4c4,
                                linear-gradient(#d6d6d6 50%, white 100%),
                                radial-gradient(center 50% -40%, radius 200%, #e6e6e6 45%, rgba(230,230,230,0) 50%);
                            -fx-background-radius: 30;
                            -fx-background-insets: 0,1,1;
                            -fx-text-fill: black;
                            -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 3, 0.0 , 0 , 1 );"""

  val menuStyle =   s"""    -fx-background-color:
                                transparent, 
                                #707070,
                                linear-gradient(#fcfcfc, #f3f3f3),
                                linear-gradient(#f2f2f2 0%, #ebebeb 49%, #dddddd 50%, #cfcfcf 100%);
                            -fx-text-fill: black;
                            -fx-font-size: 16px;"""

  val engine = browser.engine
  engine.load(browserConfigs.homepage)

  val txfUrl : TextField = new TextField {
    text  = engine.location.value
    hgrow = Priority.Always
    vgrow = Priority.Never
  }

  // include webengine so we can take this to a lib
  def navigateBack() : Unit = {
    try {
      engine.getHistory.go(-1)
    } catch {
      case e : Exception => return
    }
  }

  def navigateForward() : Unit = {
    try {
      engine.getHistory.go(1)
    } catch {
      case e : Exception => return
    }
  }

  val btnMenu = new Button {
    text = "\u2261"
    style = menuStyle
  }

  val btnFwd = new Button {
    text = ">"
    style = buttonStyle
    filterEvent(MouseEvent.Any) {
        (me: MouseEvent) =>
          me.eventType match {
              case MouseEvent.MousePressed => navigateForward()
              case _ => print("")
          }
      }
  }

  val btnBack = new Button {
    text = "<"
    style = buttonStyle
    filterEvent(MouseEvent.Any) {
        (me: MouseEvent) =>
          me.eventType match {
              case MouseEvent.MousePressed => navigateBack()
              case _ => print("")
          }
      }
  }

  val bxStatusBar = new HBox {
    children = Seq(
      txfUrl,   
      btnBack,
      btnFwd)
    hgrow = Priority.Always
    vgrow = Priority.Never 
  }

  val topBox = new HBox {
    children = Seq(
      txfUrl,
      btnBack,
      btnFwd,
      btnMenu
      )
    hgrow = Priority.Always
    vgrow = Priority.Never 
  } 

  txfUrl.onAction = handle {engine.load(parseUrl(txfUrl.text.get))}

  /*
   * make url fully qualified
   */
  def parseUrl(url : String) : String = {
    def isValid (toCheck : String) : Boolean = {
      val validStarters = List("http", "ftp")
      validStarters.map(toCheck startsWith _).contains(true)
    }

    val count = url.count(_ == '.')
    if (isValid(url)) {
      url
    }
    else if (count == 0 || url.contains(' ')) {
      browserConfigs.searchEngine + url
    }
    else if (count == 1) {
      "http://www." + url
    } else {
      "http://" + url
    }
  }

  stage = new PrimaryStage {
    title <== engine.title
    fullScreen = true
    scene = new Scene {
      fill = Color.LightGray
      root = new BorderPane {
        hgrow = Priority.Always
        vgrow = Priority.Always
        top = topBox
        center = browser
      }
    }
  }

}