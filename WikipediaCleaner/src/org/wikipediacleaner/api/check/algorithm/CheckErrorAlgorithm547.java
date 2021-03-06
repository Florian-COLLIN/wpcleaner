/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.check.algorithm;

import java.util.Collection;
import java.util.List;

import org.wikipediacleaner.api.check.CheckErrorResult;
import org.wikipediacleaner.api.data.CharacterUtils;
import org.wikipediacleaner.api.data.Namespace;
import org.wikipediacleaner.api.data.PageElementListItem;
import org.wikipediacleaner.api.data.PageElementTag;
import org.wikipediacleaner.api.data.PageElementTemplate;
import org.wikipediacleaner.api.data.PageElementTemplate.Parameter;
import org.wikipediacleaner.api.data.analysis.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementTitle;


/**
 * Algorithm for analyzing error 547 of check wikipedia project.
 * Error 547: Empty list item.
 */
public class CheckErrorAlgorithm547 extends CheckErrorAlgorithmBase {

  public CheckErrorAlgorithm547() {
    super("Empty list item");
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
    if (analysis == null) {
      return false;
    }

    // Only in main name space
    if ((analysis.getPage().getNamespace() == null) ||
        (analysis.getPage().getNamespace().intValue() != Namespace.MAIN)) {
      return false;
    }

    // Check if list items are present
    List<PageElementListItem> listItems = analysis.getListItems();
    if ((listItems == null) || (listItems.isEmpty())) {
      return false;
    }

    // Check each list item
    boolean result = false;
    String contents = analysis.getContents();
    for (PageElementListItem listItem : listItems) {

      // Check if list item has text
      boolean shouldReport = true;
      int index = listItem.getBeginIndex() + listItem.getDepth();
      while (shouldReport && (index < listItem.getEndIndex())) {
        shouldReport = CharacterUtils.isWhitespace(contents.charAt(index));
        index++;
      }

      // Filter special cases
      if (shouldReport) {
        if ((analysis.getSurroundingTag(PageElementTag.TAG_WIKI_NOWIKI, index) != null) ||
            (analysis.getSurroundingTag(PageElementTag.TAG_WIKI_SCORE, index) != null) ||
            (analysis.getSurroundingTag(PageElementTag.TAG_WIKI_SOURCE, index) != null) ||
            (analysis.getSurroundingTag(PageElementTag.TAG_WIKI_SYNTAXHIGHLIGHT, index) != null)) {
          shouldReport = false;
        }
      }
      if (shouldReport) {
        if (analysis.comments().isAt(index)) {
          shouldReport = false;
        }
      }

      // Report error
      if (shouldReport) {
        result = true;
        if (errors == null) {
          return result;
        }

        // Determine boundaries
        boolean automatic = false;
        int begin = listItem.getBeginIndex();
        int end = listItem.getEndIndex();
        boolean extended = false;
        if (end + 1 < contents.length()) {
          char nextChar = contents.charAt(end + 1);
          if (nextChar == '\n') {
            automatic = true;
            end++;
            extended = true;
          } else if (PageElementListItem.isListIndicator(nextChar)) {
            end++;
            extended = true;
          } else {
            automatic = true;
          }
        } else {
          automatic = true;
        }
        if (begin > 1) {
          char previousChar = contents.charAt(begin - 1);
          if (previousChar == '\n') {
            char previousChar2 = contents.charAt(begin - 2);
            if (previousChar2 == '\n') {
              if (!extended) {
                begin--;
                extended = true;
              }
              automatic = true;
            } else if (previousChar2 == '=') {
              PageElementTitle title = analysis.isInTitle(begin - 2);
              if (title != null) {
                automatic = true;
              }
            }
          }
        } else {
          automatic = true;
        }

        // Specific check if fix can be automatic
        if (automatic &&
            (analysis.isInImage(index) != null)) {
          automatic = false;
        }
        if (automatic) {
          // Note: due to badly written templates that requires a parameter not to be empty...
          PageElementTemplate template = analysis.isInTemplate(index);
          if (template != null) {
            Parameter param = template.getParameterAtIndex(index);
            if (param != null) {
              // TODO: be less restrictive, only if list item is alone?
              automatic = false;
            }
          }
        }

        // Report error
        CheckErrorResult errorResult = createCheckErrorResult(analysis, begin, end);
        errorResult.addReplacement("", automatic);
        errors.add(errorResult);
      }
    }
    return result;
  }

  /**
   * Automatic fixing of all the errors in the page.
   * 
   * @param analysis Page analysis.
   * @return Page contents after fix.
   */
  @Override
  protected String internalAutomaticFix(PageAnalysis analysis) {
    if (!analysis.getPage().isArticle() ||
        !analysis.getPage().isInMainNamespace()) {
      return analysis.getContents();
    }
    return fixUsingAutomaticReplacement(analysis);
  }
}
