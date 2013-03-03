/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2008  Nicolas Vervelle
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wikipediacleaner.api.check.algorithm;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.wikipediacleaner.api.check.CheckErrorResult;
import org.wikipediacleaner.api.data.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementTitle;


/**
 * Algorithm for analyzing error 25 of check wikipedia project.
 * Error 25: Headline hierarchy
 */
public class CheckErrorAlgorithm025 extends CheckErrorAlgorithmBase {

  public CheckErrorAlgorithm025() {
    super("Headline hierarchy");
  }

  /**
   * Analyze a page to check if errors are present.
   * 
   * @param pageAnalysis Page analysis.
   * @param errors Errors found in the page.
   * @return Flag indicating if the error was found.
   */
  public boolean analyze(
      PageAnalysis pageAnalysis,
      Collection<CheckErrorResult> errors) {
    if (pageAnalysis == null) {
      return false;
    }

    // Check every title
    List<PageElementTitle> titles = pageAnalysis.getTitles();
    boolean result = false;
    int previousTitleLevel = -1;
    for (PageElementTitle title : titles) {
      if ((previousTitleLevel > 0) &&
          (title.getLevel() > previousTitleLevel + 1)) {
        if (errors == null) {
          return true;
        }
        result = true;
        CheckErrorResult errorResult = createCheckErrorResult(
            pageAnalysis.getPage(), title.getBeginIndex(), title.getEndIndex());
        errors.add(errorResult);
      }
      previousTitleLevel = title.getLevel();
    }

    return result;
  }

  /**
   * Bot fixing of all the errors in the page.
   * 
   * @param analysis Page analysis.
   * @return Page contents after fix.
   */
  @Override
  public String botFix(PageAnalysis analysis) {
    String contents = analysis.getContents();
    if (!analysis.areTitlesReliable()) {
      return contents;
    }

    // Replace titles
    StringBuilder tmp = new StringBuilder();
    int lastIndex = 0;
    Vector<Integer> offsets = new Vector<Integer>();
    List<PageElementTitle> titles = analysis.getTitles();
    for (int index = 0; index < titles.size(); index++) {

      // Compute current offset
      PageElementTitle title = titles.get(index);
      offsets.setSize(title.getLevel());
      int offset = 0;
      for (Integer levelOffset : offsets) {
        if (levelOffset != null) {
          offset += levelOffset.intValue();
        }
      }

      // Replace title if needed
      if (offset > 0) {
        if (lastIndex < title.getBeginIndex()) {
          tmp.append(contents.substring(lastIndex, title.getBeginIndex()));
          lastIndex = title.getBeginIndex();
        }
        tmp.append(PageElementTitle.createTitle(title.getLevel() - offset, title.getTitle()));
        if (title.getAfterTitle() != null) {
          tmp.append(title.getAfterTitle());
        }
        lastIndex = title.getEndIndex();
      }

      // Compute level offset
      int levelOffset = Integer.MAX_VALUE;
      for (int index2 = index + 1;
           (index2 < titles.size()) && (titles.get(index2).getLevel() > title.getLevel());
           index2++) {
        levelOffset = Math.min(
            levelOffset,
            titles.get(index2).getLevel() - title.getLevel() - 1);
      }
      offsets.add(Integer.valueOf(levelOffset));
      if ((levelOffset == 0) &&
          (index + 1 < titles.size()) &&
          (titles.get(index + 1).getLevel() > title.getLevel() + 1)) {
        offsets.add(Integer.valueOf(titles.get(index + 1).getLevel() - title.getLevel() - 1));
      }
    }
    if (lastIndex == 0) {
      return contents;
    }
    if (lastIndex < contents.length()) {
      tmp.append(contents.substring(lastIndex));
    }

    return tmp.toString();
  }
}
