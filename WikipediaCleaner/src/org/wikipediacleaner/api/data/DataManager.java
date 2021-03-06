/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.utils.Configuration;


/**
 * Utility class to manage wiki data.
 */
public class DataManager {

  /**
   * @param wiki Wiki.
   * @param title Page title.
   * @param revisionId Revision id.
   * @param knownPages Already known pages.
   * @return The requested page if it exists in the known pages.
   */
  public static Page getExistingPage(
      EnumWikipedia wiki,
      String title, String revisionId,
      List<Page> knownPages) {

    // Check parameters
    if ((knownPages == null) ||
        (wiki == null) ||
        (title == null)) {
      return null;
    }

    // Check in the known pages
    for (Page page : knownPages) {
      if ((page != null) &&
          (page.getWikipedia() == wiki) &&
          (Page.areSameTitle(page.getTitle(), title)) &&
          ((revisionId == null) || (revisionId.equals(page.getRevisionId().toString())))) {
        return page;
      }
    }

    return null;
  }

  /**
   * @param wiki Wiki.
   * @param title Page title.
   * @param pageId Page id.
   * @param revisionId Revision id.
   * @param knownPages Already known pages.
   * @return The requested page.
   */
  public static Page getPage(
      EnumWikipedia wiki,
      String title, Integer pageId, String revisionId,
      List<Page> knownPages) {

    // Check in the known pages
    Page page = getExistingPage(wiki, title, revisionId, knownPages);
    if (page != null) {
      return page;
    }

    // Retrieve page
    page = new Page(wiki, title);
    page.setPageId(pageId);
    page.setRevisionId(revisionId);

    // Manage namespace
    if (page.getTitle() != null) {
      int colonIndex = page.getTitle().indexOf(':');
      if (colonIndex > 0) {
        String namespaceText = page.getTitle().substring(0, colonIndex);
        List<Namespace> namespaces = wiki.getWikiConfiguration().getNamespaces();
        if (namespaces != null) {
          for (Namespace namespace : namespaces) {
            if (namespace.isPossibleName(namespaceText)) {
              page.setNamespace(namespace.getId());
            }
          }
        }
      }
      if (page.getNamespace() == null) {
        page.setNamespace(Namespace.MAIN);
      }
    }

    // Manage comments
    Configuration config = Configuration.getConfiguration();
    Object comment = config.getPojo(
        wiki, Configuration.POJO_PAGE_COMMENTS, page.getTitle(), PageComment.class);
    if (comment instanceof PageComment) {
      page.setComment((PageComment) comment);
    }

    return page;
  }

  /**
   * Date formatter for ISO 8601 Date and Time.
   */
  private final static SimpleDateFormat iso8601DateTime = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

  /**
   * Convert a string holding a date in ISO 8601 format.
   * 
   * @param date Date in ISO 8601 format.
   * @return Date.
   * @throws ParseException Exception due to the date format.
   */
  public static Date convertIso8601DateTime(String date) throws ParseException {
    synchronized (iso8601DateTime) {
      return iso8601DateTime.parse(date);
    }
  }
}
