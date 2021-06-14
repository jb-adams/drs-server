package org.ga4gh.starterkit.drs.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.ga4gh.starterkit.common.config.ServerProps;
import org.ga4gh.starterkit.drs.config.DrsServiceProps;
import org.ga4gh.starterkit.drs.model.AccessMethod;
import org.ga4gh.starterkit.drs.model.AccessType;
import org.ga4gh.starterkit.drs.model.AccessURL;
import org.ga4gh.starterkit.drs.model.AwsS3AccessObject;
import org.ga4gh.starterkit.drs.model.ContentsObject;
import org.ga4gh.starterkit.drs.model.DrsObject;
import org.ga4gh.starterkit.drs.model.FileAccessObject;
import org.ga4gh.starterkit.drs.utils.cache.AccessCache;
import org.ga4gh.starterkit.drs.utils.cache.AccessCacheItem;
import org.springframework.beans.factory.annotation.Autowired;

public class DrsObjectTransformUtil {

    @Autowired
    ServerProps serverProps;

    @Autowired
    DrsServiceProps drsServiceProps;

    @Autowired
    AccessCache accessCache;

    /**
     * Constructs the self URI from server hostname and object id
     * @param id DrsObject identifier
     * @return self-referencing URI
     */
    public URI prepareSelfURI(String id) {
        StringBuffer uriBuffer = new StringBuffer("drs://");
        uriBuffer.append(serverProps.getHostname());
        if (!serverProps.getPublicApiPort().equals("80")) {
            uriBuffer.append(":" + serverProps.getPublicApiPort());
        }
        uriBuffer.append("/" + id.toString());
        return URI.create(uriBuffer.toString());
    }

    /**
     * Constructs the contents object list from a DrsObject's children
     * @param drsObject DrsObject with loaded children
     * @return List of contents objects derived from children
     */
    public List<ContentsObject> prepareContents(DrsObject drsObject, boolean expand) {
        List<ContentsObject> contents = new ArrayList<>();
        for (int i = 0; i < drsObject.getDrsObjectChildren().size(); i++) {
            contents.add(createContentsObject(drsObject.getDrsObjectChildren().get(i), expand));
        }
        return contents;
    }

    /**
     * Constructs a single contents object from a DrsObject
     * @param drsObject DrsObject to be converted into a ContentsObject
     * @return ContentsObject derived from the DrsObject
     */
    private ContentsObject createContentsObject(DrsObject drsObject, boolean expand) {
        ContentsObject contentsObject = new ContentsObject();
        contentsObject.setId(drsObject.getId());
        contentsObject.setDrsUri(new ArrayList<URI>(){{
            add(prepareSelfURI(drsObject.getId()));
        }});
        contentsObject.setName(drsObject.getName());

        // if 'expand' boolean is true, perform recursive function to recursively
        // convert all children DrsObjects to ContentsObjects
        if (expand) {
            List<ContentsObject> childContents = new ArrayList<>();
            for (int i = 0; i < drsObject.getDrsObjectChildren().size(); i++) {
                childContents.add(createContentsObject(drsObject.getDrsObjectChildren().get(i), expand));
            }
            contentsObject.setContents(childContents);
        }
        
        return contentsObject;
    }

    /**
     * Constructs a combined list of access methods from all different types
     * of AccessObjects (eg FileAccessObjects, AwsS3AccessObjects)
     * @param drsObject the DrsObject for which the access methods list will be constructed
     * @return list of access methods
     */
    public List<AccessMethod> prepareAccessMethods(DrsObject drsObject) {

        List<AccessMethod> accessMethods = new ArrayList<>();

        // Convert file-based access objects to AccessMethods
        // if indicated by the DrsServiceProps, return a 'file://' URL indicating
        // the direct path and/or an 'http(s)://' URL pointing to the streaming
        // endpoint
        for (FileAccessObject fileAccessObject : drsObject.getFileAccessObjects()) {
            if (drsServiceProps.getServeFileURLForFileObjects()) {
                accessMethods.add(createFileURLAccessMethodForFileObject(fileAccessObject));
            }
            if (drsServiceProps.getServeStreamURLForFileObjects()) {
                accessMethods.add(createStreamAccessMethodForFileObject(fileAccessObject));
            }
        }

        // Convert s3-based access objects to AccessMethods
        for (AwsS3AccessObject awsS3AccessObject : drsObject.getAwsS3AccessObjects()) {
            accessMethods.add(createAccessMethod(awsS3AccessObject));
        }
        
        return accessMethods;
    }

    /**
     * Construct a file:// URL for a file-based access object
     * @param fileAccessObject file-based access object
     * @return access method with a file:// URL
     */
    private AccessMethod createFileURLAccessMethodForFileObject(FileAccessObject fileAccessObject) {
        AccessMethod accessMethod = new AccessMethod();
        accessMethod.setType(AccessType.file);
        AccessURL accessURL = new AccessURL(URI.create(
            "file://" + fileAccessObject.getPath()
        ));
        accessMethod.setAccessUrl(accessURL);
        return accessMethod;
    }

    /**
     * Construct an http(s):// URL pointing to streaming endpoint for a file-based access object
     * @param fileAccessObject file-based access object
     * @return access method with a http(s):// URL
     */
    private AccessMethod createStreamAccessMethodForFileObject(FileAccessObject fileAccessObject) {
        AccessMethod accessMethod = new AccessMethod();
        accessMethod.setType(AccessType.https);

        // populate the cache with a new item containing the access ID so it
        // can be recgonized by the access endpoint
        String accessID = UUID.randomUUID().toString();
        AccessCacheItem accessCacheItem = generateAccessCacheItem(
            fileAccessObject.getDrsObject().getId(),
            accessMethod.getAccessId(),
            fileAccessObject.getPath(),
            accessMethod.getType(),
            fileAccessObject.getDrsObject().getMimeType());
        accessCache.put(fileAccessObject.getDrsObject().getId(), accessID, accessCacheItem);

        accessMethod.setAccessId(accessID);
        return accessMethod;
    }

    /**
     * Construct an s3:// URL for an s3-based access object
     * @param awsS3AccessObject S3-based access object
     * @return access method with s3:// URL
     */
    private AccessMethod createAccessMethod(AwsS3AccessObject awsS3AccessObject) {
        AccessMethod accessMethod = new AccessMethod();
        accessMethod.setType(AccessType.s3);
        accessMethod.setRegion(awsS3AccessObject.getRegion());

        AccessURL accessURL = new AccessURL(URI.create(
            "s3://" + awsS3AccessObject.getBucket() 
            + awsS3AccessObject.getKey()));
        accessMethod.setAccessUrl(accessURL);
        return accessMethod;

    }

    /**
     * Create an access cache item with the supplied parameters
     * @param objectId DrsObject identifier
     * @param accessId access id
     * @param objectPath file path/URL to byte source 
     * @param accessType path/URL type
     * @param mimeType media type
     * @return access cache item populated with the supplied parameters
     */
    private AccessCacheItem generateAccessCacheItem(String objectId, String accessId, String objectPath, AccessType accessType, String mimeType) {
        AccessCacheItem item = new AccessCacheItem();
        item.setObjectId(objectId);
        item.setAccessId(accessId);
        item.setObjectPath(objectPath);
        item.setAccessType(accessType);
        item.setMimeType(mimeType);
        return item;
    }
}
