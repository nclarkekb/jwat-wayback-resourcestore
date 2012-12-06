package org.jwat.wayback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.logging.Logger;

import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;
import org.archive.wayback.webapp.PerformanceLogger;
import org.jwat.common.RandomAccessFileInputStream;

public class JWATResourceFactory {

	private static final Logger LOGGER = Logger.getLogger(ResourceFactory.class.getName());

	public static Resource getResource(String urlOrPath, long offset) throws IOException, ResourceNotAvailableException {
		LOGGER.info("Fetching: " + urlOrPath + " : " + offset);
		try {
			if(urlOrPath.startsWith("http://")) {
				return getResource(new URL(urlOrPath), offset);
			} else {
				// assume local path:
				return getResource(new File(urlOrPath), offset);
			}
		} catch(ResourceNotAvailableException e) {
			LOGGER.warning("ResourceNotAvailable for " + urlOrPath + " " + e.getMessage());
			throw e;
		} catch(IOException e) {
			LOGGER.warning("ResourceNotAvailable for " + urlOrPath + " " + e.getMessage());
			throw e;
		}
	}

	public static Resource getResource(URL url, long offset) throws IOException, ResourceNotAvailableException {
		// TODO: allow configuration of timeouts -- now using defaults..
		long start = System.currentTimeMillis();
		JWATTimeoutConnectionFactory tarf = new JWATTimeoutConnectionFactory();
		InputStream in = tarf.getArchiveReader(url, offset);
		Resource r = JWATResource.getResource(in, offset);
		long elapsed = System.currentTimeMillis() - start;
		PerformanceLogger.noteElapsed("Http11Resource", elapsed, url.toExternalForm());
		return r;
	}

	public static Resource getResource(File file, long offset) throws IOException, ResourceNotAvailableException {
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(offset);
		if (raf.getFilePointer() != offset) {
			throw new ResourceNotAvailableException("offset is invalid");
		}
		RandomAccessFileInputStream rafin = new RandomAccessFileInputStream(raf);
		return JWATResource.getResource(rafin, offset);
	}

	/*
	String name = file.getName();
	if (name.endsWith(ArcWarcFilenameFilter.OPEN_SUFFIX)) {
		name = name.substring(0, name.length() - ArcWarcFilenameFilter.OPEN_SUFFIX.length());
	}
	if (isArc(name)) {
		ArcReader arcReader = ArcReaderFactory.getReader(rafin, 8192);
		ArcRecordBase arcRecord = arcReader.getNextRecord();
		r = JWATResource.fromArcRecord(arcReader, arcRecord);
	} else if (isWarc(name)) {
		WarcReader warcReader = WarcReaderFactory.getReader(rafin, 8192);
		WarcRecord warcRecord = warcReader.getNextRecord();
		r = JWATResource.fromWarcRecord(warcReader, warcRecord);
	} else {
		throw new ResourceNotAvailableException("Unknown extension");
	}
	*/

	/*
	private static boolean isArc(final String name) {
		return (name.endsWith(ArcWarcFilenameFilter.ARC_SUFFIX)
				|| name.endsWith(ArcWarcFilenameFilter.ARC_GZ_SUFFIX));
	}
	*/

	/*
	private static boolean isWarc(final String name) {
		return (name.endsWith(ArcWarcFilenameFilter.WARC_SUFFIX)
			|| name.endsWith(ArcWarcFilenameFilter.WARC_GZ_SUFFIX));	
	}
	*/
	
}
