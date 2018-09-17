/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.imagemanagementmodule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.im4java.core.IMOperation;
import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.config.Config;
import org.kitodo.config.enums.Parameter;

/**
 * An image conversion task. One conversion task can create multiple result
 * images with different properties. This is done in one single ImageMagick
 * call, reading and decoding the source image into memory only once.
 */
class ImageConverter {
    private static final Logger logger = LogManager.getLogger(ImageConverter.class);

    /**
     * ImageMagick file type prefix to request no file being written.
     */
    private static final String FORMAT_OFF = "NULL:";

    /**
     * ImageMagick option {@code -units}. Note that {@code -units} must be set
     * <i>before</i> the operation whose value shall be interpreted in this
     * unit.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#units"
     */
    private static final String OPTION_UNITS = "-units";

    /**
     * ImageMagick option {@code -depth}’s value {@code PixelsPerInch}.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#units"
     */
    private static final String OPTION_UNITS_TYPE_PIXELSPERINCH = "PixelsPerInch";

    /**
     * Path to read the source image from.
     */
    private final String source;

    /**
     * Conversion results to create.
     */
    private final Collection<FutureDerivative> results = new LinkedList<>();

    /**
     * Creates a new image conversion task.
     *
     * @param imageFileUri
     *            source image to convert
     */
    ImageConverter(URI imageFileUri) {
        source = uriToPath(imageFileUri);
    }

    /**
     * Defines another result of the conversion process.
     *
     * @param path
     *            output file URI
     * @return the conversion result object to define conversion properties
     */
    FutureDerivative addResult(URI path) {
        FutureDerivative result = new FutureDerivative(uriToPath(path));
        results.add(result);
        return result;
    }

    /**
     * Defines another result of the conversion process.
     *
     * @param path
     *            output file URI
     * @param resultFileFormat
     *            image format to generate
     * @return the conversion result object to define conversion properties
     */
    FutureDerivative addResult(URI path, ImageFileFormat resultFileFormat) {
        FutureDerivative result = new FutureDerivative(uriToPath(path), resultFileFormat);
        results.add(result);
        return result;
    }

    /**
     * Reads further arguments from the configuration and passes them to
     * ImageMagick. Arguments can be added to the configuration with the prefix
     * {@code ImageManagementModule.param.}.
     *
     * <p>
     * Examples:
     *
     * <p>
     * <table border=2 cellspacing=1 cellpadding=2>
     * <tr>
     * <th>Configuration entry</th>
     * <th>Arguments sent to ImageMagick</th>
     * </tr>
     * <tr>
     * <td><code>ImageManagementModule.param.limit.memory=40MB</code></td>
     * <td><code>-limit memory 40MB</code></td>
     * </tr>
     * <tr>
     * <td><code>ImageManagementModule.param.+set=date\:create</code></td>
     * <td><code>+set date:create</code></td>
     * </tr>
     * <tr>
     * <td><code>ImageManagementModule.param.quiet=</code></td>
     * <td><code>-quiet</code></td>
     * </tr>
     * </table>
     *
     * @param commandLine
     *            command line to which to add the arguments
     */
    private static void configureImageMagick(IMOperation commandLine) {
        final String configOptionsPrefix = "ImageManagementModule.param.";
        Iterator<?> keys = Config.getConfig().getKeys(configOptionsPrefix);
        while (keys.hasNext()) {
            Object keyObject = keys.next();
            if (keyObject instanceof String) {
                String key = (String) keyObject;
                String option = key.substring(configOptionsPrefix.length());
                if (!(option.startsWith("+") || option.startsWith("-"))) {
                    option = "-".concat(option);
                }
                commandLine.addRawArgs(option.split("\\."));
                //FIXME: it is not secure to allow any possible key
                String value = Config.getParameter(key);
                if (!value.isEmpty()) {
                    commandLine.addRawArgs(value);
                }
            }
        }
    }

    /**
     * Performs the conversion by calling ImageMagick.
     */
    void run() throws IOException {
        IMOperation commandLine = new IMOperation();
        configureImageMagick(commandLine);
        commandLine.addRawArgs(OPTION_UNITS, OPTION_UNITS_TYPE_PIXELSPERINCH);
        commandLine.addImage(source);
        results.forEach(result -> result.addToCommandLine(commandLine));
        commandLine.addImage(FORMAT_OFF);
        ConvertRunner convertRunner = new ConvertRunner();
        try {
            convertRunner.setSearchPath(Config.getParameter(Parameter.SEARCH_PATH));
        } catch (NoSuchElementException e) {
            logger.trace("No deviant search path configured.", e);
        }
        convertRunner.run(commandLine);
    }

    /**
     * Converts an URI to an absolute local path as String.
     *
     * @param uri
     *            input file URI
     * @return absolute local path
     */
    private static String uriToPath(URI uri) {
        return new File(uri).getAbsolutePath();
    }
}
