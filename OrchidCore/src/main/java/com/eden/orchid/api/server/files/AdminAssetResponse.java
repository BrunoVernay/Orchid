package com.eden.orchid.api.server.files;

import com.caseyjbrooks.clog.Clog;
import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.render.RenderService;
import com.eden.orchid.api.resources.resource.OrchidResource;
import com.eden.orchid.api.server.OrchidResponse;
import com.eden.orchid.api.server.OrchidView;
import com.eden.orchid.api.theme.pages.OrchidPage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

final class AdminAssetResponse {

    static OrchidResponse getResponse(OrchidContext context, File targetFile, String targetPath) {
        OrchidView view = new OrchidView(context, "Admin", new HashMap<>());

        OrchidResource res = view.getTheme().getResourceSource().getResourceEntry(context, targetPath);
        String mimeType = StaticFileResponse.mimeTypes.getOrDefault(FilenameUtils.getExtension(targetFile.getName()), "text/plain");

        Clog.i("Rendering admin File: #{$1}", targetPath);
        if (res != null) {
            if(context.isBinaryExtension(FilenameUtils.getExtension(targetFile.getName()))) {
                InputStream stream = res.getContentStream();

                return new OrchidResponse(context)
                        .contentStream(stream)
                        .mimeType(mimeType);
            }
            else {
                OrchidPage page = new OrchidPage(
                        res,
                        RenderService.RenderMode.TEMPLATE,
                        "",
                        null
                );

                return new OrchidResponse(context)
                        .mimeType(mimeType)
                        .content(page.getContent())
                        .mimeType(mimeType);
            }
        }

        return null;
    }
}
