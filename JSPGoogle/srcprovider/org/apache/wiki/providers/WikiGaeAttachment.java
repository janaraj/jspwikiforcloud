package org.apache.wiki.providers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.QueryItem;
import org.apache.wiki.WikiPage;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.providers.jpa.AttachmentEnt;
import org.apache.wiki.providers.jpa.AttachmentOneEnt;

public class WikiGaeAttachment extends AbstractWikiProvider implements
        WikiAttachmentProvider {

    private static final Log log = LogFactory.getLog(WikiGaeAttachment.class);

    private class PutAttachment extends ECommand {

        private final Attachment att;
        private final InputStream i;

        PutAttachment(Attachment att, InputStream i) {
            super(true);
            this.att = att;
            this.i = i;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            AttachmentEnt a = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndFileName", att.getParentName(),
                    att.getFileName());
            Collection<PageUtil.WikiVersion> vList;
            int version;
            if (a == null) {
                a = new AttachmentEnt();
                a.setPageName(att.getParentName());
                a.setFileName(att.getFileName());
                vList = new ArrayList<PageUtil.WikiVersion>();
                version = 0;
            } else {
                vList = PageUtil.toVersions(a);
                PageUtil.WikiVersion vx = PageUtil.findLatest(vList);
                version = vx.version;
            }
            PageUtil.WikiVersion v = new PageUtil.WikiVersion();
            PageUtil.toVersion(v, att);
            v.version = ++version;
            v.changetime = getToday();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] arr = new byte[4096];
            int rea;
            try {
                while ((rea = i.read(arr)) != -1) {
                    out.write(arr, 0, rea);
                }
            } catch (IOException e) {
                log.fatal(e);
                throw new WikiSecurityException(e.getMessage());
            }

            v.fileSize = out.size();
            vList.add(v);
            PageUtil.toBlobPage(a, vList);
            eF.persist(a);
            commit();

            AttachmentOneEnt ata = new AttachmentOneEnt();
            ata.setContent(out.toByteArray());
            ata.setAttachKey(a.getKey().getId());
            ata.setVersion(v.version);
            eF.persist(ata);
        }
    }

    @Override
    public void putAttachmentData(Attachment att, InputStream data)
            throws ProviderException, IOException {
        PutAttachment co = new PutAttachment(att, data);
        co.runCommand();
    }

    private class GetAttachment extends ECommand {

        InputStream ii = null;
        private final Attachment att;

        GetAttachment(Attachment att) {
            super(false);
            this.att = att;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            AttachmentEnt a = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndFileName", att.getParentName(),
                    att.getFileName());
            if (a == null) {
                return;
            }
            Collection<PageUtil.WikiVersion> vList = PageUtil.toVersions(a);
            PageUtil.WikiVersion vx = PageUtil.findLatest(vList);
            AttachmentOneEnt aEnt = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndVersion", a.getKey().getId(),
                    vx.version);
            ii = new ByteArrayInputStream(aEnt.getContent());
        }

    }

    @Override
    public InputStream getAttachmentData(Attachment att)
            throws ProviderException, IOException {
        GetAttachment co = new GetAttachment(att);
        co.runCommand();
        return co.ii;
    }

    private List<Attachment> getList(List oList) throws WikiSecurityException {
        List<Attachment> outList = new ArrayList<Attachment>();
        for (Object o : oList) {
            AttachmentEnt aEnt = (AttachmentEnt) o;
            Collection<PageUtil.WikiVersion> vList = PageUtil.toVersions(aEnt);
            PageUtil.WikiVersion vx = PageUtil.findLatest(vList);
            Attachment atta = new Attachment(aEnt.getPageName(),
                    aEnt.getFileName());
            PageUtil.toWikiPage(atta, vx);
            outList.add(atta);
        }
        return outList;
    }

    private class GetListAttachment extends ECommand {

        private final WikiPage page;
        Collection aList;

        GetListAttachment(WikiPage page) {
            super(false);
            this.page = page;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            // GetAttachmentsForPage
            Query q = ECommand.getQuery(eF, "GetAttachmentsForPage",
                    page.getName());
            List oList = q.getResultList();
            aList = getList(oList);
        }

    }

    @Override
    public Collection listAttachments(WikiPage page) throws ProviderException {
        GetListAttachment co = new GetListAttachment(page);
        co.runCommand();
        return co.aList;
    }

    // Do nothing now
    @Override
    public Collection findAttachments(QueryItem[] query) {
        return null;
    }

    private class GetAllAttachments extends ECommand {

        List aList;

        GetAllAttachments() {
            super(false);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            Query q = ECommand.getQuery(eF, "GetAllAttachments");
            List oList = q.getResultList();
            aList = getList(oList);
        }

    }

    /**
     * Get all attachments, ignore timestamp
     */
    @Override
    public List listAllChanged(Date timestamp) throws ProviderException {
        GetAllAttachments co = new GetAllAttachments();
        co.runCommand();
        return co.aList;
    }

    private class GetAttachmentInfo extends ECommand {
        private final WikiPage page;
        private final String name;
        private final int version;
        Attachment atta = null;

        GetAttachmentInfo(WikiPage page, String name, int version) {
            super(false);
            this.page = page;
            this.name = name;
            this.version = version;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            AttachmentEnt aEnt = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndFileName", page.getName(), name);
            if (aEnt == null) {
                return;
            }
            Collection<PageUtil.WikiVersion> vList = PageUtil.toVersions(aEnt);
            PageUtil.WikiVersion vx = PageUtil.findVersion(vList, version);
            if (vx == null) {
                return;
            }
            atta = new Attachment(aEnt.getPageName(), aEnt.getFileName());
            PageUtil.toWikiPage(atta, vx);
        }
    }

    @Override
    public Attachment getAttachmentInfo(WikiPage page, String name, int version)
            throws ProviderException {
        GetAttachmentInfo co = new GetAttachmentInfo(page, name, version);
        co.runCommand();
        return co.atta;
    }

    private class GetVersionHistory extends ECommand {
        private final Attachment att;
        List res = null;

        GetVersionHistory(Attachment att) {
            super(false);
            this.att = att;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            AttachmentEnt a = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndFileName", att.getParentName(),
                    att.getFileName());
            if (a == null) {
                return;
            }
            Collection<PageUtil.WikiVersion> vList = PageUtil.toVersions(a);
            List<Attachment> aList = new ArrayList<Attachment>();
            for (PageUtil.WikiVersion vv : vList) {
                Attachment ata = new Attachment(att.getParentName(),
                        att.getFileName());
                PageUtil.toWikiPage(ata, vv);
                aList.add(ata);
            }
            res = aList;
        }
    }

    @Override
    public List getVersionHistory(Attachment att) {
        GetVersionHistory co = new GetVersionHistory(att);
        try {
            co.runCommand();
        } catch (ProviderException e) {
            log.error("getVersionHistory",e);
            return null;
        }
        return co.res;
    }

    private class DeleteVersion extends ECommand {
        private final Attachment att;

        DeleteVersion(Attachment att) {
            super(true);
            this.att = att;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            AttachmentEnt a = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndFileName", att.getParentName(),
                    att.getFileName());
            if (a == null) {
                return;
            }
            Collection<PageUtil.WikiVersion> vList = PageUtil.toVersions(a);
            PageUtil.WikiVersion v = PageUtil.findVersion(vList,
                    att.getVersion());
            if (v == null) {
                return;
            }
            vList.remove(v);
            PageUtil.toBlobPage(a, vList);
            eF.persist(a);
            commit();
            AttachmentOneEnt aEnt = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndVersion", a.getKey().getId(),
                    att.getVersion());
            if (aEnt != null) {
                eF.remove(aEnt);
            }

        }
    }

    @Override
    public void deleteVersion(Attachment att) throws ProviderException {
        DeleteVersion co = new DeleteVersion(att);
        co.runCommand();

    }

    private class RemoveAttachment extends ECommand {

        private final Attachment att;

        RemoveAttachment(Attachment att) {
            super(true);
            this.att = att;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            AttachmentEnt a = ECommand.getSingleObject(eF,
                    "GetAttachmentForPageAndFileName", att.getParentName(),
                    att.getFileName());
            if (a == null) {
                return;
            }
            Collection<PageUtil.WikiVersion> vList = PageUtil.toVersions(a);
            eF.remove(a);
            for (PageUtil.WikiVersion v : vList) {
                AttachmentOneEnt aEnt = ECommand.getSingleObject(eF,
                        "GetAttachmentForPageAndVersion", a.getKey().getId(),
                        v.version);
                eF.remove(aEnt);
            }
        }

    }

    @Override
    public void deleteAttachment(Attachment att) throws ProviderException {
        RemoveAttachment com = new RemoveAttachment(att);
        com.runCommand();
    }

    private class MoveAttachment extends ECommand {
        private final String oldParent;
        private final String newParent;

        MoveAttachment(String oldParent, String newParent) {
            super(true);
            this.oldParent = oldParent;
            this.newParent = newParent;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            // for some reason I cannot do it at one iteration
            // so sequence : find all, if empty then break; rename and again
            while (true) {
                Query q = ECommand.getQuery(eF, "GetAttachmentsForPage",
                        oldParent);
                List oList = q.getResultList();
                if (oList.isEmpty()) {
                    break;
                }
                AttachmentEnt aEnt = (AttachmentEnt) oList.get(0);
                aEnt.setPageName(newParent);
                eF.persist(aEnt);
                commit();
            }
        }

    }

    @Override
    public void moveAttachmentsForPage(String oldParent, String newParent)
            throws ProviderException {
        MoveAttachment com = new MoveAttachment(oldParent, newParent);
        com.runCommand();
    }

}
