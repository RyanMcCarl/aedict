/**
 *     Aedict - an EDICT browser for Android
 Copyright (C) 2009 Martin Vysny
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.baka.aedict;

import java.util.Formatter;

import android.app.Application;

/**
 * The main application class.
 * 
 * @author Martin Vysny
 */
public class AedictApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if (instance != null) {
			throw new IllegalStateException("Not a singleton");
		}
		instance = this;
	}

	private static AedictApp instance;

	/**
	 * Returns the application instance.
	 * 
	 * @return the instance.
	 */
	public static AedictApp getApp() {
		if (instance == null) {
			throw new IllegalStateException("Not yet initialized");
		}
		return instance;
	}

	/**
	 * Formats given string using {@link Formatter} and returns it.
	 * 
	 * @param resId
	 *            the string id
	 * @param args
	 *            the parameters.
	 * @return a formatted string.
	 */
	public static String format(final int resId, Object... args) {
		final String formatStr = getApp().getString(resId);
		return new Formatter().format(formatStr, args).toString();
	}

}
