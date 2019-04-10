/*******************************************************************************
 * Copyright 2019 University of Stuttgart, Germany
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package ogss.main.frontend

import java.io.File

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import org.scalatest.junit.JUnitRunner

import ogss.frontend.common.ParseException
import ogss.main.CommandLine

/**
 * Contains generic parser tests based on src/test/resources/frontend directory.
 * @author Timm Felden
 */
@RunWith(classOf[JUnitRunner])
class GenericFrontendTest extends FunSuite {

  CommandLine.exit = { s ⇒ fail(s) }
  private def check(file : File) = CommandLine.main(Array[String](
    "build",
    file.getAbsolutePath,
    "-o", "/tmp/gen"
  ))

  def fail[E <: Exception](f : ⇒ Unit)(implicit manifest : scala.reflect.Manifest[E]) : E = try {
    f;
    fail(s"expected ${manifest.runtimeClass.getName()}, but no exception was thrown");
  } catch {
    case e : TestFailedException ⇒ throw e
    case e : E ⇒
      println(e.getMessage()); e
    case e : Throwable ⇒ e.printStackTrace(); assert(e.getClass() === manifest.runtimeClass); null.asInstanceOf[E]
  }

  def succeedOn(file : File) {
    test("succeed on " + file.getName()) { check(file) }
  }

  def failOn(file : File) {
    test("fail on " + file.getName()) {
      fail[ParseException] {
        check(file)
      }
    }
  }

  for (
    f ← new File("src/test/resources/frontend").listFiles() if f.isFile()
  ) succeedOn(f)
  for (
    f ← new File("src/test/resources/frontend/fail").listFiles() if f.isFile()
  ) failOn(f)
}
