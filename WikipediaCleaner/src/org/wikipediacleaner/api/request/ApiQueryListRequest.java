/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2012  Nicolas Vervelle
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

package org.wikipediacleaner.api.request;


/**
 * Interface for MediaWiki API list query requests.
 */
public class ApiQueryListRequest extends ApiRequest {

  // ==========================================================================
  // API properties
  // ==========================================================================

  /**
   * Property for List.
   */
  public final static String PROPERTY_LIST = "list";

  /**
   * Property value for List / Back links.
   */
  public final static String PROPERTY_LIST_BACKLINKS = "backlinks";

  /**
   * Property value for List / Random pages.
   */
  public final static String PROPERTY_LIST_RANDOM = "random";

  /**
   * Property value for List / Raw watch list.
   */
  public final static String PROPERTY_LIST_WATCHLISTRAW = "watchlistraw";
}
