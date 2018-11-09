package test;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.get.impl.util.ResourceTraversor;

import javax.jcr.query.Query;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

@SlingServlet(paths = "/content/modules", selectors = "query", extensions = "json")
@Properties({
        @Property(name = "service.description", value = "Query servlet for modules"),
        @Property(name = "service.vendor", value = "Red Hat")
})
public class ModuleQueryServlet extends SlingSafeMethodsServlet {
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resolver = request.getResourceResolver();
        Iterator<Resource> resources = resolver.findResources(
                "SELECT * FROM [pantheon:modules] as mod where mod.[jcr:content] like '%" + request.getParameter("q") + "%'",
                Query.JCR_SQL2);

        response.setContentType("text/json");

        final JsonGenerator generator = Json.createGenerator(response.getWriter());
        generator.writeStartArray();

        resources.forEachRemaining(it -> {
            generator.write(new ResourceTraversor(-1, -1, it, true).getJSONObject());
        });
        generator.writeEnd();
        generator.flush();

        response.flushBuffer();
    }
}