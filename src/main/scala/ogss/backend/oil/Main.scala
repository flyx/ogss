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
package ogss.backend.oil

import java.io.File

import ogss.backend.common.DefaultBackEnd
import ogss.oil.Field
import ogss.oil.OGFile

/**
 * A back-end that produces an .oil-file, i.e. the oil-file created to communicate between back-end and front-end is
 * just written to disk.
 *
 * @author Timm Felden
 */
final class Main extends DefaultBackEnd {

  /**
   * The name of this back-end as per command line interface.
   */
  def name = "OIL"

  /**
   * The human-readable description of this back-end.
   */
  def description = "OGSS Intermediate Language"

  override def setIR(IR : OGFile) {
    this.IR = IR;
  }
  var IR : OGFile = _;

  override def make() {
    val f =
      if (files.outPath.isDirectory() || !files.outPath.getName.endsWith(".oil")) new File(files.outPath, "out.oil")
      else files.outPath
    f.getParentFile.mkdirs()
    f.createNewFile()
    IR.changePath(f.toPath)
    IR.close()
  }

  override protected def defaultValue(f : Field) : String = ???
}
