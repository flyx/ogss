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
package ogss.backend.java

trait EnumMaker extends AbstractBackEnd {
  abstract override def make {
    super.make

    for (t ← enums) {
      val out = files.open(s"${name(t)}.java")

      // package
      out.write(s"""package ${this.packageName};

public enum ${name(t)} {
  ${
        // TODO comments!
        t.values.map(id ⇒ escaped(capital(id.name))).sortWith(
          (l, r) ⇒ l.length() < r.length() || (l.length() == r.length() && l.compareTo(r) < 0)
        ).mkString("", ",\n  ", ";")
      }
}""");
      out.close()
    }
  }
}
