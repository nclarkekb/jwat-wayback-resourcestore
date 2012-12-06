package org.jwat.wayback;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.LocationDBResourceStore;
import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDB;

public class JWATLocalResourceStore implements ResourceStore {

	private static final Logger LOGGER = Logger.getLogger(LocationDBResourceStore.class.getName());

	private ResourceFileLocationDB db = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceStore#retrieveResource(org.archive.wayback.core.SearchResult)
	 */
	public Resource retrieveResource(CaptureSearchResult result) throws ResourceNotAvailableException {
		// extract ARC filename
		String fileName = result.getFile();
		if (fileName == null || fileName.length() < 1) {
			throw new ResourceNotAvailableException("No ARC/WARC name in search result...");
		}

		String urls[];
		try {
			urls = db.nameToUrls(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new ResourceNotAvailableException(e1.getLocalizedMessage());
		}
		if (urls == null || urls.length == 0) {
			LOGGER.warning("Unable to locate(" + fileName + ")");
			throw new ResourceNotAvailableException("Unable to locate(" + fileName + ")");
		}
		
		final long offset = result.getOffset();

		Resource r = null;
		for (String url : urls) {
			try {
				r = JWATResourceFactory.getResource(url, offset);				
			} catch (IOException e) {
				LOGGER.warning("Unable to retrieve resource from " + url);
			}
			if (r != null) {
				break;
			}
		}
		if (r == null) {
			throw new ResourceNotAvailableException("Unable to retrieve");
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceStore#shutdown()
	 */
	public void shutdown() throws IOException {
		db.shutdown();
	}

	/**
	 * @return the ResourceFileLocationDB used by this ResourceStore
	 */
	public ResourceFileLocationDB getDb() {
		return db;
	}

	/**
	 * @param db the ResourceFileLocationDB to use with this ResourceStore
	 */
	public void setDb(ResourceFileLocationDB db) {
		this.db = db;
	}

}
