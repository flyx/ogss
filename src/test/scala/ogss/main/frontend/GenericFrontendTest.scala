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
import scala.reflect.ClassTag
import scala.io.Source

/**
 * Contains generic parser tests based on src/test/resources/frontend directory.
 *
 * @note it is silently assumed, that files are self-contained and are either
 * SKilL or SIDL
 *
 * @author Timm Felden
 */
@RunWith(classOf[JUnitRunner])
class GenericFrontendTest extends FunSuite {
  class CLIError(msg : String) extends Error(msg);

  CommandLine.exit = { s ⇒
    // if commandline check fails, thats also fine
    throw new CLIError(s)
  }

  private def lang(file : File) = file.getName.split('.').last.toLowerCase

  private def check(file : File, language : String) =
    CommandLine.main(Array[String](
      "build",
      file.getAbsolutePath,
      "-o", "/tmp/gen",
      "-L", language
    ))

  def succeedOn(file : File) {
    test("read " + file.getName()) {
      try {
        check(file, lang(file))

      } catch {
        case e : CLIError ⇒ fail(e.getMessage)
      }
    }

    test("loop " + file.getName()) {
      lang(file) match {
        case "skill" ⇒ try {
          // path 1
          CommandLine.main(Array[String](
            "build",
            file.getAbsolutePath,
            "-o", "/tmp/gen",
            "-L", "skill"
          ))

          // path 2
          CommandLine.main(Array[String](
            "build",
            file.getAbsolutePath,
            "-o", "/tmp/gen2",
            "-L", "sidl"
          ))

          CommandLine.main(Array[String](
            "build",
            "/tmp/gen2/specification.sidl",
            "-o", "/tmp/gen2",
            "-L", "skill"
          ))

          // compare
          assert(Source.fromFile("/tmp/gen/specification.skill").getLines().mkString
            === Source.fromFile("/tmp/gen2/specification.skill").getLines().mkString)
        } catch {
          case e : CLIError ⇒ fail(e.getMessage)
        }
        case "sidl" ⇒ try {
          // path 1
          CommandLine.main(Array[String](
            "build",
            file.getAbsolutePath,
            "-o", "/tmp/gen",
            "-L", "sidl"
          ))

          // path 2
          CommandLine.main(Array[String](
            "build",
            file.getAbsolutePath,
            "-o", "/tmp/gen2",
            "-L", "skill"
          ))

          CommandLine.main(Array[String](
            "build",
            "/tmp/gen2/specification.skill",
            "-o", "/tmp/gen2",
            "-L", "sidl"
          ))

          // compare
          assert(Source.fromFile("/tmp/gen/specification.sidl").getLines().mkString
            === Source.fromFile("/tmp/gen2/specification.sidl").getLines().mkString)
        } catch {
          case e : CLIError ⇒ fail(e.getMessage)
        }
      }
    }
  }

  def failOn(file : File) {
    test("fail on " + file.getName()) {
      try {
        check(file, "skill")
        fail("expected an exception to be thrown")
      } catch {
        case e : CLIError ⇒ // success
      }
    }
  }

  for (
    f ← new File("src/test/resources/frontend").listFiles().sortBy(_.getName) if f.isFile() && f.getName.startsWith("complete")
  ) succeedOn(f)
//  for (
//    f ← new File("src/test/resources/frontend").listFiles().sortBy(_.getName) if f.isFile()
//  ) succeedOn(f)
//  for (
//    f ← new File("src/test/resources/frontend/fail").listFiles().sortBy(_.getName) if f.isFile()
//  ) failOn(f)
}
