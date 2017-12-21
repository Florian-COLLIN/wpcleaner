/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.check.algorithm;

import java.awt.ComponentOrientation;
import java.util.Collection;
import java.util.List;

import org.wikipediacleaner.api.check.CheckErrorResult;
import org.wikipediacleaner.api.check.CheckErrorResult.ErrorLevel;
import org.wikipediacleaner.api.data.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementTag;
import org.wikipediacleaner.api.data.PageElementTag.Parameter;
import org.wikipediacleaner.i18n.GT;


/**
 * Algorithm for analyzing error 525 of check wikipedia project.
 * Error 525: Useless span tag
 */
public class CheckErrorAlgorithm525 extends CheckErrorAlgorithmBase {

  public CheckErrorAlgorithm525() {
    super("Useless span tag");
  }

  /**
   * Analyze a page to check if errors are present.
   * 
   * @param analysis Page analysis.
   * @param errors Errors found in the page.
   * @param onlyAutomatic True if analysis could be restricted to errors automatically fixed.
   * @return Flag indicating if the error was found.
   */
  @Override
  public boolean analyze(
      PageAnalysis analysis,
      Collection<CheckErrorResult> errors, boolean onlyAutomatic) {
    if ((analysis == null) || (analysis.getPage() == null)) {
      return false;
    }
    if (!analysis.getPage().isArticle()) {
      return false;
    }

    // Analyze each tag
    List<PageElementTag> tags = analysis.getCompleteTags(PageElementTag.TAG_HTML_SPAN);
    if ((tags == null) || tags.isEmpty()) {
      return false;
    }
    boolean result = false;
    String contents = analysis.getContents();
    int lastIndex = 0;
    for (PageElementTag tag : tags) {
      // Decide if tag is useful
      boolean isUseless = true;
      boolean onlyUselessParameter = true;
      ErrorLevel level = ErrorLevel.ERROR;
      for (int numParam = 0; numParam < tag.getParametersCount(); numParam++) {
        Parameter param = tag.getParameter(numParam);
        boolean isParameterUseless = false;
        String value = param.getTrimmedValue();
        if ((value != null) && (!value.isEmpty())) {
          String lang = analysis.getWikipedia().getSettings().getLanguage();
          if ("lang".equals(param.getName()) && (lang != null) && lang.equalsIgnoreCase(value)) {
            // useful
          } else if ("dir".equals(param.getName())) {
            ComponentOrientation dir = analysis.getWikipedia().getSettings().getComponentOrientation();
            if (("ltr".equalsIgnoreCase(value) && (dir == ComponentOrientation.LEFT_TO_RIGHT)) ||
                ("rtl".equalsIgnoreCase(value) && (dir == ComponentOrientation.RIGHT_TO_LEFT))) {
              // useful
            } else {
              isUseless = false;
            }
          } else if ("class".equals(param.getName()) && "cx-segment".equals(param.getValue())) {
            // useless: Content Translation tool garbage
            isParameterUseless = true;
          } else if ("data-segmentid".equals(param.getName())) {
            // useless: Content Translation tool garbage
            isParameterUseless = true;
          } else if ("contenteditable".equals(param.getName())) {
            // useless: Content Translation tool garbage
            isParameterUseless = true;
          } else if ("class".equals(param.getName()) ||
                     "id".equals(param.getName())) {
            level = ErrorLevel.WARNING;
          } else {
            isUseless = false;
          }
        }
        if (!isParameterUseless) {
          onlyUselessParameter = false;
        }
      }
      if (!tag.isComplete()) {
        isUseless = true;
      }

      if (isUseless && (tag.getBeginIndex() >= lastIndex)) {
        if (errors == null) {
          return true;
        }
        result = true;
        lastIndex = tag.getCompleteEndIndex();

        // Create error
        CheckErrorResult errorResult = createCheckErrorResult(
            analysis, tag.getCompleteBeginIndex(), tag.getCompleteEndIndex(), level);
        if (tag.isFullTag() || !tag.isComplete()) {
          errorResult.addReplacement("");
        } else {
          String replacement = contents.substring(
              tag.getValueBeginIndex(), tag.getValueEndIndex());
          PageElementTag refTag = analysis.isInTag(
              tag.getCompleteEndIndex(), PageElementTag.TAG_WIKI_REF);
          if ((refTag != null) && (refTag.isEndTag())) {
            replacement = replacement.trim();
          }
          errorResult.addReplacement(
              replacement,
              GT._("Remove {0} tags", PageElementTag.TAG_HTML_SPAN),
              onlyUselessParameter);
        }
        errors.add(errorResult);
      }
    }

    return result;
  }

  /**
   * Automatic fixing of some errors in the page.
   * 
   * @param analysis Page analysis.
   * @return Page contents after fix.
   */
  @Override
  protected String internalAutomaticFix(PageAnalysis analysis) {
    return fixUsingAutomaticReplacement(analysis);
  }
}
