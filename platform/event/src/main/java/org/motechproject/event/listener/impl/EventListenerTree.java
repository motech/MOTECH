package org.motechproject.event.listener.impl;

import org.motechproject.event.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Implementation of the {@link org.motechproject.event.listener.impl.EventListenerRegistry} interface.
 * Listeners are stored as a tree.
 */
public class EventListenerTree {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerTree.class);

    private static final String SPLIT_REGEX = "\\.";

    private List<EventListenerTree> children = new ArrayList<>();
    private EventListenerTree parent;

    private String pathElement;
    private Set<EventListener> listeners;
    private Set<EventListener> wildcardListeners;

    public EventListenerTree() {
        this("*", null);
    }

    public EventListenerTree(String pathElement, EventListenerTree parent) {
        this.pathElement = pathElement;
        this.parent = parent;
    }

    /**
     * Returns the name of the pathElement in a tree, which is created
     * by splitting the subject.
     *
     * @return the name of the pathElement
     */
    public String getPathElement() {
        return pathElement;
    }

    /**
     * Returns the subject, which listener subscribes to. It is built by walk up the tree
     * appending names of pathElements.
     *
     * @return the subject the listener subscribes to
     */
    public String getSubject() {
        if (parent == null) {
            return "";
        }

        String parentSubject = parent.getSubject();
        if ("".equals(parentSubject)) {
            return pathElement;
        } else {
            return parentSubject + "." + pathElement;
        }
    }

    /**
     * @see org.motechproject.event.listener.EventListenerRegistryService#registerListener(org.motechproject.event.listener.EventListener, String)
     */
    public void addListener(EventListener listener, String subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Cannot add listener for null subject");
        }

        int asteriskLocation = subject.indexOf('*');
        if (asteriskLocation != -1 && (asteriskLocation + 1) != subject.length()) {
            throw new IllegalArgumentException("Wildcard must be last element of subject: " + subject);
        }

        if (subject.contains("..")) {
            throw new IllegalArgumentException("Subject can not contain an empty path segment: " + subject);
        }

        // Split the subject into it's path components
        String[] path = subject.split(SPLIT_REGEX);

        if (path[path.length - 1].contains("*") && path[path.length - 1].length() > 1) {
            throw new IllegalArgumentException("Wildcard can not be mixed with characters");
        }

        if ("*".equals(subject)) {
            addListener(listener);
            return;
        }

        EventListenerTree child = getChild(path[0]);
        if (child == null) {
            child = new EventListenerTree(path[0], this);
            addChild(child);
        }

        child.addListener(listener, path, 0);
    }

    private void addListener(EventListener listener, String[] path, int pathLevel) {
        // I've walked to the end of the path. Assign this listener to this node
        if ((pathLevel + 1) == path.length) {
            addListener(listener);
            return;
        }

        // If the next step is the end of the path and it's a wildcard save listener here
        if ((pathLevel + 2) == path.length && "*".equals(path[path.length - 1])) {
            addWildcardListener(listener);
            return;
        }

        EventListenerTree child = getChild(path[pathLevel + 1]);
        if (child == null) {
            child = new EventListenerTree(path[pathLevel + 1], this);
            addChild(child);
        }

        child.addListener(listener, path, (pathLevel + 1));
    }

    /**
     * @see org.motechproject.event.listener.EventListenerRegistryService#getListeners(String)
     */
    public Set<EventListener> getListeners(String subject) {
        // Split the subject into it's path components
        String[] path = subject.split(SPLIT_REGEX);

        Set<EventListener> allListeners = new HashSet<>();
        if (isRootNode() && listeners != null) {
            allListeners.addAll(listeners);
        }
        EventListenerTree child = getChild(path[0]);
        if (child == null) {
            return allListeners;
        }

        allListeners.addAll(child.getListeners(path, 0));
        return allListeners;
    }

    private boolean isRootNode() {
        return "*".equals(pathElement);
    }

    private Set<EventListener> getListeners(String[] path, int pathLevel) {
        Set<EventListener> ret;

        if ((pathLevel + 1) == path.length) {
            return getAllListeners();
        }

        EventListenerTree child = getChild(path[pathLevel + 1]);
        if (child == null) {
            return getWildcardListeners();
        }

        ret = child.getListeners(path, (pathLevel + 1));
        ret.addAll(getWildcardListeners());

        return ret;
    }

    /**
     * @see org.motechproject.event.listener.EventListenerRegistryService#hasListener(String)
     */
    public boolean hasListener(String subject) {
        // Split the subject into it's path components
        String[] path = subject.split(SPLIT_REGEX);

        if (isRootNode() && listeners != null && !listeners.isEmpty()) {
            return true;
        }
        EventListenerTree child = getChild(path[0]);
        return child != null && child.hasListener(path, 0);

    }

    private boolean hasListener(String[] path, int pathLevel) {
        if (hasWildcardListeners()) {
            return true;
        }

        if ((pathLevel + 1) == path.length) {
            return listeners != null && !listeners.isEmpty();
        }

        EventListenerTree child = getChild(path[pathLevel + 1]);
        return child != null && child.hasListener(path, (pathLevel + 1));

    }

    /**
     * @see org.motechproject.event.listener.EventListenerRegistryService#getListenerCount(String)
     */
    public int getListenerCount(String subject) {
        // Split the subject into it's path components
        String[] path = subject.split(SPLIT_REGEX);

        EventListenerTree child = getChild(path[0]);
        if (child == null) {
            return 0;
        }

        return child.getListenerCount(path, 0);
    }

    private int getListenerCount(String[] path, int pathLevel) {
        int ret = 0;

        if ((pathLevel + 1) == path.length) {
            return getAllListeners().size();
        }

        EventListenerTree child = getChild(path[pathLevel + 1]);
        if (child == null) {
            return getWildcardListeners().size();
        }

        ret = getWildcardListeners().size();

        return ret + child.getListeners(path, (pathLevel + 1)).size();
    }

    private Set<EventListener> getListeners() {
        Set<EventListener> ret = new HashSet<EventListener>();

        if (listeners == null) {
            listeners = new HashSet<EventListener>();
        }

        ret.addAll(listeners);

        return ret;
    }

    private Set<EventListener> getAllListeners() {
        Set<EventListener> ret = new HashSet<EventListener>();

        ret.addAll(getListeners());
        ret.addAll(getWildcardListeners());

        return ret;
    }

    private Set<EventListener> getWildcardListeners() {
        Set<EventListener> ret = new HashSet<EventListener>();

        if (wildcardListeners == null) {
            wildcardListeners = new HashSet<EventListener>();
        }

        ret.addAll(wildcardListeners);

        return ret;
    }

    private void addListener(EventListener listener) {
        if (listeners == null) {
            listeners = new HashSet<EventListener>();
        }

        // Don't allow duplicate listener registrations Set will handle this for me, but then I can't log it
        if (!listeners.contains(listener)) {
            listeners.add(listener); // Add the listener to the list
        } else {
            LOGGER.info(String.format("Ignoring second request to register listener %s for subject %s",
                    listener.getIdentifier(), getSubject()));
        }
    }

    private void addWildcardListener(EventListener listener) {
        if (wildcardListeners == null) {
            wildcardListeners = new HashSet<EventListener>();
        }

        // Don't allow duplicate listener registrations Set will handle this for me, but then I can't log it
        if (!wildcardListeners.contains(listener)) {
            wildcardListeners.add(listener); // Add the listener to the list
        } else {
            LOGGER.info(String.format("Ignoring second request to register wildcardListeners %s for subject %s.*",
                    listener.getIdentifier(), getSubject()));
        }
    }

    private boolean hasWildcardListeners() {
        return wildcardListeners != null && !wildcardListeners.isEmpty();
    }

    private EventListenerTree getChild(String pathElement) {
        EventListenerTree ret = null;

        if (children != null) {
            for (EventListenerTree n : children) {
                if (n.getPathElement().equals(pathElement)) {
                    ret = n;
                    break;
                }
            }
        }

        return ret;
    }

    private void addChild(EventListenerTree child) {
        if (children == null) {
            children = new ArrayList<EventListenerTree>();
        }

        children.add(child);
    }

    /**
     * @see org.motechproject.event.listener.EventListenerRegistryService#clearListenersForBean(String)
     */
    public void removeAllListeners(String beanName) {

        for (Iterator<EventListenerTree> listenerIterator = children.iterator(); listenerIterator.hasNext();) {
            EventListenerTree child = listenerIterator.next();
            if (child.containsListenersForBeanName(beanName) && child.allListenersEmpty()) {
                listenerIterator.remove();
            }
        }
    }

    private boolean allListenersEmpty() {

        if (children.size() == 0) {
            return this.getAllListeners().size() == 0;
        } else {
            for (Iterator<EventListenerTree> listenerIterator = children.iterator(); listenerIterator.hasNext();) {
                EventListenerTree child = listenerIterator.next();
                if (!child.allListenersEmpty() || isEmpty(wildcardListeners)) {
                    return false;
                } else {
                    listenerIterator.remove();
                }
            }
        }
        return true;
    }

    private boolean containsListenersForBeanName(String beanName) {
        boolean removed = false;

        if (listeners != null) {
            for (Iterator<EventListener> listenerIterator = listeners.iterator(); listenerIterator.hasNext();) {
                EventListener nextListener = listenerIterator.next();
                if (nextListener.getIdentifier().equals(beanName)) {
                    listenerIterator.remove();
                    removed = true;
                }
            }
        }

        if (wildcardListeners != null) {
            for (Iterator<EventListener> listenerIterator = wildcardListeners.iterator(); listenerIterator.hasNext();) {
                EventListener nextListener = listenerIterator.next();
                if (nextListener.getIdentifier().equals(beanName)) {
                    listenerIterator.remove();
                    removed = true;
                }
            }
        }
        for (EventListenerTree childTree : children) {
            if (childTree.containsListenersForBeanName(beanName)) {
                removed = true;
            }
        }
        return removed;
    }
}
