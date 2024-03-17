package common.geom;

import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.*;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class SridUtils {

  private final static CRSFactory crsFactory = new CRSFactory();
  private final static CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
  
  private final static Map<String, CoordinateReferenceSystem> cache = new HashMap<>();
  
  private SridUtils() {
  }

  /**
   *
   * @param <T>
   * @param geometry
   * @param targetSrid
   * @return
   */
  public static synchronized <T extends Geometry> T  transform(T geometry, int targetSrid) {
    String sourceCRSCode = "EPSG:" + geometry.getSRID();
    String targetCRSCode = "EPSG:" + targetSrid;
    CoordinateReferenceSystem sourceCRS = getCrs(sourceCRSCode);
    CoordinateReferenceSystem targetCRS = getCrs(targetCRSCode);
    CoordinateTransform transform = ctFactory.createTransform(sourceCRS, targetCRS);
    T result = transformGeometry(geometry, transform);
    result.setSRID(targetSrid);
    return result;
  }

  /**
   *
   * @param code
   * @return
   */
  private static synchronized CoordinateReferenceSystem getCrs(String code) {
    if (!cache.containsKey(code)) {
      CoordinateReferenceSystem result = crsFactory.createFromName(code);
      cache.put(code, result);
    }
    CoordinateReferenceSystem result = cache.get(code);
    return result;
  }

  /**
   *
   */
  public static void init() {
    try {
      GeometryFactory f = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
      Point p = f.createPoint(new Coordinate(-121.43, 36.37));
      transform(p, 3857);
    } catch (Exception ex) {
      throw new RuntimeException("Error initializing SridUtils", ex);
    }
  }

  public static Point transform(Point p, int targetSrid) {
    return (Point) transform((Geometry) p, targetSrid);
  }

  /**
   *
   * @param geometry
   * @param transform
   * @return
   */
  private static <T extends Geometry> T transformGeometry(T geometry, CoordinateTransform transform) {
    PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
    int srid = Integer.parseInt(transform.getTargetCRS().getName().replace("EPSG:", ""));
    GeometryFactory factory = new GeometryFactory(precisionModel, srid);
    if (geometry instanceof Point) {
      return (T) transformPoint((Point) geometry, transform, factory);
    
    } else if (geometry instanceof LinearRing) {
      return (T) transformLineRing((LinearRing) geometry, transform, factory);
    } else if (geometry instanceof LineString) {
      return (T) transformLineString((LineString) geometry, transform, factory);
    } else if (geometry instanceof Polygon) {
      return (T) transformPolygon((Polygon) geometry, transform, factory);
    } else {
      throw new RuntimeException("Invalid geometry");
    }
  }
  
  /**
   * 
   * @param lineString
   * @param transform
   * @param factory
   * @return 
   */
  private static LineString transformLineString(LineString lineString, //
          CoordinateTransform transform, GeometryFactory factory) {
    Coordinate[] sourceCoordinates = lineString.getCoordinates();
    Coordinate[] targetCoordinates = new Coordinate[sourceCoordinates.length];
    for (int i = 0; i < sourceCoordinates.length; i++) {
      ProjCoordinate sourceCoord = new ProjCoordinate( //
              sourceCoordinates[i].x, sourceCoordinates[i].y);
      ProjCoordinate targetCoord = new ProjCoordinate();
      transform.transform(sourceCoord, targetCoord);
      targetCoordinates[i] = new Coordinate(targetCoord.x, targetCoord.y);
    }

    return factory.createLineString(targetCoordinates);
  }

  /**
   *
   * @param polygon
   * @param transform
   * @param factory
   * @return
   */
  private static Polygon transformPolygon(Polygon polygon, CoordinateTransform transform, GeometryFactory factory) {
    LinearRing shell = transformLineRing(polygon.getExteriorRing(), transform, factory);
    LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      holes[i] = transformLineRing(polygon.getInteriorRingN(i), transform, factory);
    }
    return factory.createPolygon(shell, holes);
  }
  
  /**
   * 
   * @param exteriorRing
   * @param transform
   * @param factory
   * @return 
   */
  private static LinearRing transformLineRing(LinearRing exteriorRing, // 
          CoordinateTransform transform, GeometryFactory factory) {
    Coordinate[] sourceCoordinates = exteriorRing.getCoordinates();
    Coordinate[] targetCoordinates = new Coordinate[sourceCoordinates.length];
    for (int i = 0; i < sourceCoordinates.length; i++) {
      ProjCoordinate sourceCoord = new ProjCoordinate(sourceCoordinates[i].x, sourceCoordinates[i].y);
      ProjCoordinate targetCoord = new ProjCoordinate();
      transform.transform(sourceCoord, targetCoord);
      targetCoordinates[i] = new Coordinate(targetCoord.x, targetCoord.y);
    }
    return factory.createLinearRing(targetCoordinates);
  }

  /**
   *
   * @param point
   * @param transform
   * @param factory
   * @return
   */
  private static Point transformPoint(Point point, // 
          CoordinateTransform transform, GeometryFactory factory) {
    ProjCoordinate sourceCoord = new ProjCoordinate(point.getX(), point.getY());
    ProjCoordinate targetCoord = new ProjCoordinate();
    transform.transform(sourceCoord, targetCoord);
    return factory.createPoint(new Coordinate(targetCoord.x, targetCoord.y));
  }
}
