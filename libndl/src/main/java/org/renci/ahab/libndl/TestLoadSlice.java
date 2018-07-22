package org.renci.ahab.libndl;

import org.renci.ahab.libtransport.*;
import org.renci.ahab.libtransport.util.SSHAccessTokenFileFactory;
import org.renci.ahab.libtransport.util.UtilTransportException;
import org.renci.ahab.libtransport.xmlrpc.XMLRPCProxyFactory;

import java.net.URL;

public class TestLoadSlice {
  static String sshKey = "~/.ssh/id_rsa";
  static String pemLocation = "~/.ssl/geni-yuanjuny.pem";
  static String exogenism = "https://geni.renci.org:11443/orca/xmlrpc";
  static protected SliceAccessContext<SSHAccessToken> sctx;
  static protected ISliceTransportAPIv1 sliceProxy;


  public static void main(String[] args) {
    loadSlice();
    System.out.println("log");
  }

  private static void loadSlice() {
    //SSH context
    sctx = new SliceAccessContext<>();
    try {
      SSHAccessTokenFileFactory fac;
      fac = new SSHAccessTokenFileFactory(sshKey + ".pub", false);
      SSHAccessToken t = fac.getPopulatedToken();
      sctx.addToken("root", "root", t);
      sctx.addToken("root", t);
    } catch (UtilTransportException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    sliceProxy = getSliceProxy(pemLocation, pemLocation, exogenism);
    try {
      Slice slice = Slice.loadManifestFile(sliceProxy, "sdx-yaoyj11");
      System.out.println(slice.getComputeNodes());
    }catch (Exception e){

      e.printStackTrace();
    }
  }


  public static ISliceTransportAPIv1 getSliceProxy(String pem, String key, String controllerUrl) {
    ISliceTransportAPIv1 sliceProxy = null;
    try {
      //ExoGENI controller context
      ITransportProxyFactory ifac = new XMLRPCProxyFactory();
      TransportContext ctx = new PEMTransportContext("", pem, key);
      sliceProxy = ifac.getSliceProxy(ctx, new URL(controllerUrl));
    } catch (Exception e) {
      e.printStackTrace();
      assert (false);
    }
    return sliceProxy;
  }

}
