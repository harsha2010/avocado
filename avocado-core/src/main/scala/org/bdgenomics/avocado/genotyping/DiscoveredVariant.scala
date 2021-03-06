/**
 * Licensed to Big Data Genomics (BDG) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The BDG licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.avocado.genotyping

import org.bdgenomics.adam.models.ReferenceRegion
import org.bdgenomics.formats.avro.Variant

/**
 * Companion object for creating DiscoveredVariants.
 */
private[genotyping] object DiscoveredVariant {

  /**
   * @param variant The variant to convert.
   * @return Returns a case class-based representation of the variant.
   */
  def apply(variant: Variant): DiscoveredVariant = {
    new DiscoveredVariant(variant.getContigName,
      variant.getStart.toInt,
      variant.getReferenceAllele,
      Some(variant.getAlternateAllele))
  }

  /**
   * @param contigName The contig this variant is on.
   * @param start The position this variant starts at.
   * @param referenceAllele The reference allele this variant varies from.
   * @param alternateAllele The substituted allele.
   * @return Returns a discovered variant with a defined alternate allele.
   */
  def apply(
    contigName: String,
    start: Int,
    referenceAllele: String,
    alternateAllele: String): DiscoveredVariant = {
    new DiscoveredVariant(contigName, start, referenceAllele, Some(alternateAllele))
  }

  /**
   * @param rr The region where this variant occurs.
   * @return Returns a discovered variant with no alternate allele. Uses a
   *   generic reference allele of "N".
   */
  def apply(rr: ReferenceRegion): DiscoveredVariant = {
    new DiscoveredVariant(rr.referenceName, rr.start.toInt, "N", None)
  }
}

/**
 * A variant site and alleles.
 *
 * @param contigName The contig this variant is on.
 * @param start The position this variant starts at.
 * @param referenceAllele The reference allele this variant varies from.
 * @param alternateAllele The substituted allele.
 */
case class DiscoveredVariant(
    contigName: String,
    start: Int,
    referenceAllele: String,
    alternateAllele: Option[String]) {

  /**
   * @return True if this is a symbolic model of a non-ref allele.
   */
  def isNonRefModel: Boolean = alternateAllele.isEmpty

  lazy val end: Int = start + referenceAllele.length

  /**
   * @return Returns an avro representation of this variant.
   */
  def toVariant: Variant = {
    val builder = Variant.newBuilder
      .setContigName(contigName)
      .setStart(start.toLong)
      .setEnd(end.toLong)
      .setReferenceAllele(referenceAllele)

    alternateAllele.foreach(aa => {
      builder.setAlternateAllele(aa)
    })

    builder.build
  }

  def overlaps(v: DiscoveredVariant): Boolean = {
    contigName == v.contigName && start < v.end && end > v.start
  }

  def overlaps(rr: ReferenceRegion): Boolean = {
    contigName == rr.referenceName && start < rr.end && end > rr.start
  }
}
