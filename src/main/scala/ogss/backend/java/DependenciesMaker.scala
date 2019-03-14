/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.backend.java

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.security.MessageDigest

/**
 * creates copies of required jars in $outPath
 * @author Timm Felden
 */
trait DependenciesMaker extends AbstractBackEnd {
  abstract override def make {
    super.make

    // safe unnecessary overwrites that cause race conditions on parallel builds anyway
    if (!skipDependencies)
      for (jar ← jars) {
        this.getClass.synchronized({

          val out = new File(depsPath, jar);
          out.getParentFile.mkdirs();

          if (try {
            !out.exists() || !commonJarSum(jar).equals(sha256(out.toPath()))
          } catch {
            case e : IOException ⇒
              false // just continue
          }) {
            Files.deleteIfExists(out.toPath)
            try {
              Files.copy(new File("deps/" + jar).toPath, out.toPath)
            } catch {
              case e : NoSuchFileException ⇒
                throw new IllegalStateException("deps directory apparently inexistent.\nWas looking for " + new File("deps/" + jar).getAbsolutePath, e)
            }
          }
        })
      }
  }

  val jars = Seq("ogss.common.jvm.jar", "ogss.common.java.jar")
  lazy val commonJarSum = jars.map { s ⇒ (s -> sha256("deps/" + s)) }.toMap

  final def sha256(name : String) : String = sha256(new File(name).toPath)
  @inline final def sha256(path : Path) : String = {
    val bytes = Files.readAllBytes(path)
    MessageDigest.getInstance("SHA-256").digest(bytes).map("%02X".format(_)).mkString
  }
}
