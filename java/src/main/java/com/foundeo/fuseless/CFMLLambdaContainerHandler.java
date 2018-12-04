package com.foundeo.fuseless;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.AwsProxyExceptionHandler;
import com.amazonaws.serverless.proxy.AwsProxySecurityContextWriter;
import com.amazonaws.serverless.proxy.ExceptionHandler;
import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.ResponseWriter;
import com.amazonaws.serverless.proxy.SecurityContextWriter;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.internal.servlet.*;

import com.amazonaws.services.lambda.runtime.Context;


import org.apache.log4j.Logger;


import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

import java.io.*;


public class CFMLLambdaContainerHandler<RequestType, ResponseType>
        extends AwsLambdaServletContainerHandler<RequestType, ResponseType, AwsProxyHttpServletRequest, AwsHttpServletResponse> {

    private static final Logger LOG = Logger.getLogger(CFMLLambdaContainerHandler.class);

    /**
     * Returns a new instance of an CFMLLambdaContainerHandler initialized to work with <code>AwsProxyRequest</code>
     * and <code>AwsProxyResponse</code> objects.
     *
     * @return a new instance of <code>CFMLLambdaContainerHandler</code>
     *
     * @throws ContainerInitializationException Throws this exception if we fail to initialize the Spark container.
     * This could be caused by the introspection used to insert the library as the default embedded container
     */
    public static CFMLLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> getAwsProxyHandler()
            throws ContainerInitializationException {
        CFMLLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> newHandler = new CFMLLambdaContainerHandler<>(AwsProxyRequest.class,
                                                                                         AwsProxyResponse.class,
                                                                                         new AwsProxyHttpServletRequestReader(),
                                                                                         new AwsProxyHttpServletResponseWriter(),
                                                                                         new AwsProxySecurityContextWriter(),
                                                                                         new AwsProxyExceptionHandler()
                                                                                         );

        newHandler.setLogFormatter(new ApacheCombinedServletLogFormatter<>());

        return newHandler;
    }


    public CFMLLambdaContainerHandler(Class<RequestType> requestTypeClass,
                                       Class<ResponseType> responseTypeClass,
                                       RequestReader<RequestType, AwsProxyHttpServletRequest> requestReader,
                                       ResponseWriter<AwsHttpServletResponse, ResponseType> responseWriter,
                                       SecurityContextWriter<RequestType> securityContextWriter,
                                       ExceptionHandler<ResponseType> exceptionHandler)
            throws ContainerInitializationException {
        super(requestTypeClass, responseTypeClass, requestReader, responseWriter, securityContextWriter, exceptionHandler);
        


        

        
    }

    

    @Override
    protected AwsHttpServletResponse getContainerResponse(AwsProxyHttpServletRequest request, CountDownLatch latch) {
        return new AwsHttpServletResponse(request, latch);
    }


    @Override
    protected void handleRequest(AwsProxyHttpServletRequest httpServletRequest, AwsHttpServletResponse httpServletResponse, Context lambdaContext)
            throws Exception {
                
        httpServletRequest.setServletContext(new ServletContextWrapper(getServletContext()));
        RequestWrapper req = new RequestWrapper((javax.servlet.http.HttpServletRequest)httpServletRequest);
        req.setAttribute("lambdaContext", lambdaContext);
        try {
            LOG.debug("CFMLLambdaContainerHandler handleRequest: " + req.getRequestURI());
            StreamLambdaHandler.getCFMLServlet().service(req, httpServletResponse);
            
        } catch (Throwable t) {
            t.printStackTrace();
            LOG.error("CFMLLambdaContainerHandler Servlet Request Threw Exception: ");
            LOG.error(t);
            for (StackTraceElement st: t.getStackTrace()) {
                LOG.error("STE:" + st.toString());               
            }
        }
    }


    @Override
    public void initialize()
            throws ContainerInitializationException {
        
                
        
        
    }
}