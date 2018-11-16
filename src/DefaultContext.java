/*
 * DefaultContext.java
 */

import java.awt.Color;

/**
 * The default data context for geometry-definition commands.
 */

public class DefaultContext {

   // if you change these names, you may also need to change
   // some duplicates in the save code.  but, for backward
   // compatibility you should really avoid changing anything.

   public static Context create() {
      Context c = new Context();

      c.dict.put("dup",new Command.Dup());
      c.dict.put("exch",new Command.Exch());
      c.dict.put("pop",new Command.Pop());
      c.dict.put("index",new Command.Index());
      c.dict.put("def",new Command.Def());

      c.dict.put("add",new Command.Add());
      c.dict.put("sub",new Command.Sub());
      c.dict.put("mul",new Command.Mul());
      c.dict.put("div",new Command.Div());
      c.dict.put("neg",new Command.Neg());

      c.dict.put("[",new Command.ArrayStart());
      c.dict.put("]",new Command.ArrayEnd());

      c.dict.put("include",new Command.Include());

      c.dict.put("true", Boolean.TRUE );
      c.dict.put("false",Boolean.FALSE);

      c.dict.put("viewinfo",new Command.ViewInfo());
      c.dict.put("drawinfo",new Command.DrawInfo());
      //
      c.dict.put("noblock3",new Struct.DimensionMarker(3));
      c.dict.put("noblock4",new Struct.DimensionMarker(4));

      c.dict.put("null",new Command.Null()); // can't just put constant null, that would look like undefined
      c.dict.put("?",new Command.Null());
      c.dict.put("edge",new Command.Edge());
      c.dict.put("cedge",new Command.ColorEdge());
      c.dict.put("face",new Command.Face());
      c.dict.put("shape",new Command.Shape());
      c.dict.put("texture",new Command.Texture());
      c.dict.put("shapetexture",new Command.ShapeTexture());
      c.dict.put("glue",new Command.Glue());
      c.dict.put("unglue",new Command.Unglue());

      c.dict.put("nomove",new Command.NoUserMove());
      c.dict.put("idealize",new Command.Idealize());
      c.dict.put("copy",new Command.Copy());
      c.dict.put("place", new Command.Place());
      c.dict.put("translate",new Command.Translate());
      c.dict.put("scale",new Command.Scale());
      c.dict.put("aligncenter",new Command.AlignCenter());
      c.dict.put("rotate",new Command.Rotate(false));
      c.dict.put("altrot",new Command.Rotate(true));
      c.dict.put("glass",new Command.Glass());
      c.dict.put("shapecolor",new Command.ShapeColor());
      c.dict.put("facecolor",new Command.FaceColor());
      c.dict.put("edgecolor",new Command.EdgeColor());
      c.dict.put("facetexture",new Command.FaceTexture());
      c.dict.put("genpoly",new Command.GeneralPolygon());
      c.dict.put("etr",new Command.EdgeToRadius());
      c.dict.put("eth",new Command.EdgeToHeight());
      c.dict.put("polygon",new Command.Polygon(GeomUtil.OFFSET_REG));
      c.dict.put("altpoly",new Command.Polygon(GeomUtil.OFFSET_ALT));
      c.dict.put("product",new Command.Product());
      c.dict.put("rect",new Command.Rect());
      c.dict.put("prism",new Command.Prism());
      c.dict.put("frustum",new Command.Frustum());
      c.dict.put("cone",new Command.Cone());
      c.dict.put("polygon-antiprism",new Command.Antiprism(GeomUtil.OFFSET_REG));
      c.dict.put("altpoly-antiprism",new Command.Antiprism(GeomUtil.OFFSET_ALT));
      c.dict.put("trainpoly",new Command.TrainPoly());

      c.dict.put("texturecolor",new Command.TextureColor());
      c.dict.put("union",new Command.Union());
      c.dict.put("merge",new Command.Merge());
      c.dict.put("normalize",new Command.Normalize());
      c.dict.put("lift",new Command.Lift());
      c.dict.put("project",new Command.Project());

      c.dict.put("newtrack",new Command.NewTrack());
      c.dict.put("newtrack2",new Command.NewTrack2());
      c.dict.put("platformstyle",new Command.Set(Track.class,"setPlatformStyle"));
      c.dict.put("platformthickness",new Command.Set(Track.class,"setPlatformThickness"));
      c.dict.put("platformwidth",new Command.Set(Track.class,"setPlatformWidth"));
      c.dict.put("platformcolor",new Command.Set(Track.class,"setPlatformColor",Color.class));
      c.dict.put("platformcorner",new Command.Set(Track.class,"setPlatformCorner",Boolean.TYPE));
      c.dict.put("ramptriangle",new Command.Set(Track.class,"setRampTriangle",Boolean.TYPE));
      c.dict.put("pylonwidth",new Command.Set(Track.class,"setPylonWidth"));
      c.dict.put("pylonsides",new Command.Set(Track.class,"setPylonSides"));
      c.dict.put("pylonoffset",new Command.Set(Track.class,"setPylonOffset",Boolean.TYPE));
      c.dict.put("track",new Command.AddTrack());
      c.dict.put("platforms",new Command.AddPlatforms());
      c.dict.put("platform", new Command.AddPlatform(false));
      c.dict.put("rplatform",new Command.AddPlatform(true));
      c.dict.put("roundshape",new Command.RoundShape());
      // maybe add thinshape some day
      c.dict.put("ramp",new Command.AddRamp());
      c.dict.put("pylon", new Command.AddPylon(false));
      c.dict.put("bpylon",new Command.AddPylon(true));
      c.dict.put("train",new Command.TrainCtor());
      c.dict.put("car",   new Command.CarCtor(false));
      c.dict.put("lencar",new Command.CarCtor(true));

      c.dict.put("mat3",new Command.ConstructSetColor(Mat.Mat3.class));
      c.dict.put("mat4",new Command.ConstructSetColor(Mat.Mat4.class));
      c.dict.put("oldmat4",new Command.ConstructSetColor(Mat.OldMat4.class));
      c.dict.put("nomat",new Command.Construct(Mat.NoMat.class));
      //
      c.dict.put("meshring3",new Command.MeshRing3());
      c.dict.put("meshring4",new Command.MeshRing4());
      c.dict.put("meshsphere4",new Command.MeshSphere4());
      c.dict.put("meshframe4",new Command.MeshFrame4());
      c.dict.put("meshringframe4",new Command.MeshRingFrame4());
      c.dict.put("meshcube4",new Command.MeshCube4());
      c.dict.put("groundcube3",new Command.GroundCube3());
      c.dict.put("groundcube4",new Command.GroundCube4());
      //
      c.dict.put("heightconst",new Command.HeightConst());
      c.dict.put("heightpower",new Command.HeightPower());
      c.dict.put("mountain",new Command.HeightMountain());
      c.dict.put("heightmax",new Command.HeightMaxN());
      c.dict.put("colorconst",new Command.ColorConst());
      c.dict.put("colordir",new Command.ColorDir());
      c.dict.put("colorblend",new Command.ColorBlend());
      //
      c.dict.put("compass3",new Command.Compass(3));
      c.dict.put("compass4",new Command.Compass(4));
      c.dict.put("grid3",new Command.Grid(3));
      c.dict.put("grid4",new Command.Grid(4));
      c.dict.put("ground",new Command.Ground());
      c.dict.put("groundtexture",new Command.GroundTexture());
      c.dict.put("monolith",new Command.Monolith());
      c.dict.put("horizon",new Command.Horizon());
      c.dict.put("sky",new Command.Sky());
      c.dict.put("sun",new Command.Sun());

      c.dict.put("TM_SQUARE",new Double(Train.TM_SQUARE));
      c.dict.put("TM_ROUND", new Double(Train.TM_ROUND ));
      c.dict.put("TM_ROTATE",new Double(Train.TM_ROTATE));

      c.dict.put("PS_SQUARE",    new Double(Platform.PS_SQUARE    ));
      c.dict.put("PS_ROUND",     new Double(Platform.PS_ROUND     ));
      c.dict.put("PS_ROUND_MORE",new Double(Platform.PS_ROUND_MORE));
      c.dict.put("PS_THIN",      new Double(Platform.PS_THIN      ));
      c.dict.put("PS_THIN_ROUND",new Double(Platform.PS_THIN_ROUND));

      c.dict.put("PROJ_NONE",   new Double(Vec.PROJ_NONE   ));
      c.dict.put("PROJ_NORMAL", new Double(Vec.PROJ_NORMAL ));
      c.dict.put("PROJ_ORTHO",  new Double(Vec.PROJ_ORTHO  ));
      c.dict.put("PROJ_PERSPEC",new Double(Vec.PROJ_PERSPEC));

      c.dict.put("x",new Double(0));
      c.dict.put("y",new Double(1));
      c.dict.put("z",new Double(2));
      c.dict.put("w",new Double(3));

      c.dict.put("x+",new Double(0));
      c.dict.put("x-",new Double(1));
      c.dict.put("y+",new Double(2));
      c.dict.put("y-",new Double(3));
      c.dict.put("z+",new Double(4));
      c.dict.put("z-",new Double(5));
      c.dict.put("w+",new Double(6));
      c.dict.put("w-",new Double(7));

      // have to be a little careful because these are 4D,
      // but they'll work in the situations I use them in.
      //
      c.dict.put("X+",new double[] { 1, 0, 0, 0 });
      c.dict.put("X-",new double[] {-1, 0, 0, 0 });
      c.dict.put("Y+",new double[] { 0, 1, 0, 0 });
      c.dict.put("Y-",new double[] { 0,-1, 0, 0 });
      c.dict.put("Z+",new double[] { 0, 0, 1, 0 });
      c.dict.put("Z-",new double[] { 0, 0,-1, 0 });
      c.dict.put("W+",new double[] { 0, 0, 0, 1 });
      c.dict.put("W-",new double[] { 0, 0, 0,-1 });

      c.dict.put("red",Color.red);
      c.dict.put("green",Color.green);
      c.dict.put("blue",Color.blue);
      c.dict.put("cyan",Color.cyan);
      c.dict.put("magenta",Color.magenta);
      c.dict.put("yellow",Color.yellow);
      c.dict.put("orange",new Color(255,128,0)); // Color.orange is (255,200,0)
      c.dict.put("gray",Color.gray);
      c.dict.put("white",Color.white);
      c.dict.put("brown",new Color(128,96,0));

      return c;
   }

}

