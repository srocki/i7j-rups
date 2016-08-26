/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 2007-2015 iText Group NV
 * Authors: Bruno Lowagie et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.rups.view;

import com.itextpdf.rups.model.SwingHelper;

import java.awt.Color;
import java.io.*;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * A Class that is used for displaying logger messages to a JTextPane.
 */
public class Console implements Observer {

    /**
     * Single Console instance.
     */
    private static Console console = null;

    /**
     * The StyleContext for the Console.
     */
    ConsoleStyleContext styleContext = new ConsoleStyleContext();

    /**
     * The text area to which everything is written.
     */
    private final JTextPane textArea = new JTextPane(new DefaultStyledDocument(styleContext));

    private static final int MAX_TEXT_AREA_SIZE = 8192;

    /**
     * Creates a new Console object.
     */
    private Console(boolean pluginMode) {
        // Add a scrolling text area
        textArea.setEditable(false);
    }

    /**
     * Console is a Singleton class: you can only get one Console.
     */
    public static synchronized Console getInstance() {
        if (console == null) {
            console = new Console(false);
        }
        return console;
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (RupsMenuBar.CLOSE.equals(obj)) {
            textArea.setText("...document is closed.\n");
        }
        if (RupsMenuBar.OPEN.equals(obj)) {
            textArea.setText("...document is open.\n");
        }
    }

    private void updateTextPane(final String msg, final String type) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                try {
                    Document doc = textArea.getDocument();
                    AttributeSet attset = styleContext.getStyle(type);
                    if (doc.getLength() + msg.length() > MAX_TEXT_AREA_SIZE) {
                        textArea.setText("...\n");
                    }
                    doc.insertString(doc.getLength(), msg, attset);
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                } catch (BadLocationException ignored) {
                }
            }
        }, true);
    }

    /**
     * Get the JTextArea to which everything is written.
     */
    public JTextPane getTextArea() {
        return textArea;
    }

    static class ConsoleOutputStream extends OutputStream {

        private String type;

        ConsoleOutputStream(String type) {
            this.type = type;
        }

        @Override
        public void write(final int b) throws IOException {
            Console.getInstance().updateTextPane(String.valueOf((char) b), type);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            Console.getInstance().updateTextPane(new String(b, off, len), type);
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
    }

    /**
     * The style context defining the styles of each type of PrintStream.
     */
    static class ConsoleStyleContext extends StyleContext {

        /**
         * A Serial Version UID.
         */
        private static final long serialVersionUID = 7253870053566811171L;
        /**
         * The name of the Style used for Custom messages
         */
        public static final String INFO = "Info";
        /**
         * The name of the Style used for System.out
         */
        public static final String DEBUG = "Debug";
        /**
         * The name of the Style used for System.err
         */
        public static final String ERROR = "Error";

        /**
         * Creates the style context for the Console.
         */
        public ConsoleStyleContext() {
            super();
            Style root = getStyle(DEFAULT_STYLE);
            Style s = addStyle(INFO, root);
            StyleConstants.setForeground(s, Color.BLACK);
            s = addStyle(DEBUG, root);
            StyleConstants.setForeground(s, Color.GREEN);
            s = addStyle(ERROR, root);
            StyleConstants.setForeground(s, Color.RED);
        }
    }
}
