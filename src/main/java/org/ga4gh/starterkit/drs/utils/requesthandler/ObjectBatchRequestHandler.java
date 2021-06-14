package org.ga4gh.starterkit.drs.utils.requesthandler;

import org.ga4gh.starterkit.common.requesthandler.RequestHandler;
import org.ga4gh.starterkit.drs.model.DrsCart;
import org.ga4gh.starterkit.drs.model.DrsObject;
import org.ga4gh.starterkit.drs.model.Selection;
import org.ga4gh.starterkit.drs.utils.hibernate.DrsHibernateUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ObjectBatchRequestHandler implements RequestHandler<DrsCart> {

    @Autowired
    private DrsHibernateUtil hibernateUtil;

    private Selection selection;

    public ObjectBatchRequestHandler() {

    }

    public ObjectBatchRequestHandler prepare(Selection selection) {
        this.selection = selection;
        return this;
    }

    public DrsCart handleRequest() {
        DrsCart drsCart = new DrsCart();

        int requested = 0;
        int loaded = 0;
        int unloaded = 0;
        for (String objectId : selection.getSelection()) {
            requested++;
            DrsObject drsObject = hibernateUtil.loadDrsObject(objectId, true);

            if (drsObject != null) {
                loaded++;
                drsCart.getDrsObjects().put(objectId, drsObject);
            } else {
                unloaded++;
                drsCart.getUnloadedDrsObjects().put(objectId, 404);
            }
        }

        drsCart.getSummary().setRequested(requested);
        drsCart.getSummary().setLoaded(loaded);
        drsCart.getSummary().setUnloaded(unloaded);

        return drsCart;
    }
}
