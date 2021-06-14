package org.ga4gh.starterkit.drs.utils.requesthandler;

import org.ga4gh.starterkit.common.exception.ResourceNotFoundException;
import org.ga4gh.starterkit.common.requesthandler.RequestHandler;
import org.ga4gh.starterkit.drs.model.DrsObject;
import org.ga4gh.starterkit.drs.utils.DrsObjectTransformUtil;
import org.ga4gh.starterkit.drs.utils.hibernate.DrsHibernateUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Request handling logic for loading a DRSObject and formatting it according
 * to the DRS specification
 */
public class ObjectRequestHandler implements RequestHandler<DrsObject> {

    @Autowired
    DrsObjectTransformUtil drsObjectTransformUtil;

    @Autowired
    DrsHibernateUtil hibernateUtil;

    private String objectId;

    private boolean expand;

    /* Constructors */

    /**
     * Instantiate a new ObjectRequestHandler
     */
    public ObjectRequestHandler() {
        
    }

    /**
     * Prepares the request handler with input params from the controller function
     * @param objectId DrsObject identifier
     * @param expand boolean indicating whether to return nested/recursive bundles under 'contents'
     * @return the prepared request handler
     */
    public ObjectRequestHandler prepare(String objectId, boolean expand) {
        this.objectId = objectId;
        this.expand = expand;
        return this;
    }

    /**
     * Obtains information about a DRSObject and formats it to the DRS specification
     */
    public DrsObject handleRequest() {

        // Get DrsObject from db
        DrsObject drsObject = hibernateUtil.loadDrsObject(objectId, true);
        if (drsObject == null) {
            throw new ResourceNotFoundException("No DrsObject found by id: " + objectId);
        }

        // post query prep of response
        drsObject.setSelfURI(drsObjectTransformUtil.prepareSelfURI(objectId));
        drsObject.setContents(drsObjectTransformUtil.prepareContents(drsObject, expand));
        drsObject.setAccessMethods(drsObjectTransformUtil.prepareAccessMethods(drsObject));
        return drsObject;
    }
}
