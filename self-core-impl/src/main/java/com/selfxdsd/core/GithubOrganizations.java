package com.selfxdsd.core;

import com.selfxdsd.api.Organization;
import com.selfxdsd.api.Organizations;
import com.selfxdsd.api.User;
import com.selfxdsd.api.storage.Storage;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Iterator;

/**
 * A Github Provider Organizations.
 *
 * @author criske
 * @version $Id$
 * @since 0.0.9
 */
final class GithubOrganizations implements Organizations {

    /**
     * Github's JSON Resources.
     */
    private final JsonResources resources;

    /**
     * Organizations URI.
     */
    private final URI uri;

    /**
     * Current authenticated User.
     */
    private final User owner;

    /**
     * Storage used by Organization Repos.
     */
    private final Storage storage;

    /**
     * Ctor.
     *
     * @param resources Github's JSON Resources.
     * @param githubUri Github's base API Uri..
     * @param owner Current authenticated User.
     * @param storage Storage used by Organization Repos.
     */
    GithubOrganizations(final JsonResources resources,
                        final URI githubUri,
                        final User owner,
                        final Storage storage) {
        final String githubUriStr = githubUri.toString();
        String slash = "/";
        if(githubUriStr.endsWith("/")){
            slash = "";
        }
        this.uri = URI.create(githubUriStr + slash + "user/orgs");
        this.resources = resources;
        this.owner = owner;
        this.storage = storage;
    }

    @Override
    public Iterator<Organization> iterator() {
        final Resource resource = this.resources.get(this.uri);
        final JsonArray organizations;
        final int statusCode = resource.statusCode();
        switch (statusCode) {
            case HttpURLConnection.HTTP_OK:
                organizations = resource.asJsonArray();
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new IllegalStateException("Current User is "
                    + "not authenticated.");
            default:
                throw new IllegalStateException("Unable to fetch Github "
                    + "organizations for current User. Expected 200 OK, "
                    + "but got: " + statusCode);
        }
        return organizations
            .stream()
            .map(o -> (Organization) new GithubOrganization(
                this.owner,
                (JsonObject) o,
                this.resources,
                this.storage))
            .iterator();
    }
}
