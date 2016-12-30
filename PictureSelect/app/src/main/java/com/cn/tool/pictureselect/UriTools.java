package com.cn.tool.pictureselect;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * Uri工具类
 * 
 * <p>
 * 包含两个实用工具<br>
 * {@link #getImageAbsolutePath(Activity, Uri)} 根据Uri获取图片路径（支持4.4及其以上版本）<br>
 * {@link #getUriFromPath(String)} 通过文件路径获取该文件的Uri
 * </p>
 * 
 * @author weir 2015.9.6
 **/
public class UriTools {

	private static final String PATH_DOCUMENT = "document";

	private static String getDocumentId(Uri documentUri) {
		final List<String> paths = documentUri.getPathSegments();
		if (paths.size() < 2) {
			throw new IllegalArgumentException("Not a document: " + documentUri);
		}
		if (!PATH_DOCUMENT.equals(paths.get(0))) {
			throw new IllegalArgumentException("Not a document: " + documentUri);
		}
		return paths.get(1);
	}

	private static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	private static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	/**
	 * 根据Uri获取图片绝对路径（支持4.4及其以上版本）
	 * 
	 * <p>
	 * 由于主App目前暂时不允许使用API
	 * KITKAT及其以上的SDK，因此这里将SDK的部分方法摘出来放到该类中，且原判断条件以注释形式展示，现在去掉了后半段判断逻辑
	 * </p>
	 * 
	 * @param imageUri
	 * @return filePath or null
	 */
	@TargetApi(19)
	public static String getImageAbsolutePath(Activity context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		try {
			// if (android.os.Build.VERSION.SDK_INT >=
			// android.os.Build.VERSION_CODES.KITKAT &&
			// DocumentsContract.isDocumentUri(context, imageUri))
			if (android.os.Build.VERSION.SDK_INT >= 19) {
				if (isExternalStorageDocument(imageUri)) {
					String docId = getDocumentId(imageUri);
					String[] split = docId.split(":");
					String type = split[0];
					if ("primary".equalsIgnoreCase(type)) {
						return Environment.getExternalStorageDirectory() + "/"
								+ split[1];
					}
				} else if (isDownloadsDocument(imageUri)) {
					String id = getDocumentId(imageUri);
					Uri contentUri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"),
							Long.valueOf(id));
					return getDataColumn(context, contentUri, null, null);
				} else if (isMediaDocument(imageUri)) {
					String docId = getDocumentId(imageUri);
					String[] split = docId.split(":");
					String type = split[0];
					Uri contentUri = null;
					if ("image".equals(type)) {
						contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}
					String selection = MediaStore.Images.Media._ID + "=?";
					String[] selectionArgs = new String[] { split[1] };
					return getDataColumn(context, contentUri, selection,
							selectionArgs);
				}
			} // MediaStore (and general)
			else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
				// Return the remote address
				if (isGooglePhotosUri(imageUri))
					return imageUri.getLastPathSegment();
				return getDataColumn(context, imageUri, null, null);
			}
			// File
			else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
				return imageUri.getPath();
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	/**
	 * 由Uri获取真实路径
	 */
	public static String getRealPathFromURI(Activity context, Uri contentUri) {
		String res = null;
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
		if (cursor == null) {
			return contentUri.getPath();
		} else {
			if (cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				res = cursor.getString(column_index);
			}
			//加入未查询成功则通过此方式获取
			if (TextUtils.isEmpty(res)) {
				res = getImageAbsolutePath(context, contentUri);
			}
			cursor.close();
		}
		return res;
	}


	/**
	 * 通过文件路径获取该文件的Uri
	 * 
	 * @param absolutePath
	 * @return uri or null
	 **/
	public static Uri getUriFromPath(String absolutePath) {
		try {
			return Uri.fromFile(new File(absolutePath));
		} catch (Exception e) {
			return null;
		}
	}
}
