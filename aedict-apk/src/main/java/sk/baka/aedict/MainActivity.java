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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Provides means to search the edict dictionary file.
 * 
 * @author Martin Vysny
 */
public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			JpUtils.initialize(getClassLoader());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		setContentView(R.layout.main);
		final Button jpSearch = (Button) findViewById(R.id.jpSearch);
		final EditText jpSearchEdit = (EditText) findViewById(R.id.jpSearchEdit);
		jpSearchEdit.setOnEditorActionListener(new SearchJpText());
		jpSearch.setOnClickListener(new SearchJpText());
		final Button engSearch = (Button) findViewById(R.id.engSearch);
		final EditText engSearchEdit = (EditText) findViewById(R.id.engSearchEdit);
		engSearchEdit.setOnEditorActionListener(new SearchEngText());
		engSearch.setOnClickListener(new SearchEngText());
		// check for dictionary file
		if (!DownloadEdictTask.isComplete()) {
			final StatFs stats = new StatFs("/sdcard");
			final long free = stats.getBlockSize() * stats.getAvailableBlocks();
			final StringBuilder msg = new StringBuilder(
					getString(R.string.edict_missing_download));
			if (free < 20000000) {
				msg.append(getString(R.string.warning_less_than_20mb_free));
			}
			showYesNoDialog(msg.toString(),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							new DownloadEdictTask(MainActivity.this).execute();
						}

					});
		}
	}

	private class SearchJpText implements TextView.OnEditorActionListener,
			View.OnClickListener {
		public void onClick(View v) {
			final EditText jpSearchEdit = (EditText) findViewById(R.id.jpSearchEdit);
			final CheckBox jpExactMatch = (CheckBox) findViewById(R.id.jpExactMatch);
			final SearchQuery q = new SearchQuery();
			q.isJapanese = true;
			final String romaji = jpSearchEdit.getText().toString();
			q.query = new String[] { JpUtils.toHiragana(romaji),
					JpUtils.toKatakana(romaji) };
			q.matcher = jpExactMatch.isChecked() ? MatcherEnum.ExactMatchEng
					: MatcherEnum.SubstringMatch;
			performSearch(q);
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			onClick(v);
			return true;
		}
	}

	private class SearchEngText implements TextView.OnEditorActionListener,
			View.OnClickListener {
		public void onClick(View v) {
			final EditText engSearchEdit = (EditText) findViewById(R.id.engSearchEdit);
			final CheckBox engExactMatch = (CheckBox) findViewById(R.id.engExactMatch);
			final SearchQuery q = new SearchQuery();
			q.isJapanese = false;
			q.query = new String[] { engSearchEdit.getText().toString() };
			q.matcher = engExactMatch.isChecked() ? MatcherEnum.ExactMatchEng
					: MatcherEnum.SubstringMatch;
			performSearch(q);
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			onClick(v);
			return true;
		}
	}

	private void performSearch(final SearchQuery query) {
		final Intent intent = new Intent(this, ResultActivity.class);
		query.putTo(intent);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuItem item = menu.add(R.string.cleanup);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				cleanup();
				return true;
			}
		});
		return true;
	}

	private void showYesNoDialog(final String message,
			final DialogInterface.OnClickListener yesListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.yes, yesListener);
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showErrorDialog(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(R.string.error);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.create().show();
	}

	private void showInfoDialog(final int messageRes) {
		showInfoDialog(getString(messageRes));
	}
	private void showInfoDialog(final String message) {
		final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
	}

	private void cleanup() {
		showYesNoDialog(AedictApp.format(R.string.deleteEdictFiles, MiscUtils
				.getLength(new File(DownloadEdictTask.BASE_DIR)) / 1024),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							MiscUtils.deleteDir(new File(
									DownloadEdictTask.BASE_DIR));
							showInfoDialog(R.string.data_files_removed);
						} catch (Exception ex) {
							Log.e(MainActivity.class.getSimpleName(), ex
									.toString(), ex);
							showErrorDialog(getString(R.string.failed_to_clean_files) + ex);
						}
					}

				});
	}
}