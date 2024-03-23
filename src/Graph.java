import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

public class Graph {

  private File cities;
  private File roads;
  private Map<String, City> nameCityMap; // le nom de la ville correspond a l'objet City
  private Map<Integer, City> idCityMap; // id de la ville correspond a l'objet City
  private Map<City, Set<Road>> outputRoads;
  private Scanner scanner;


  public Graph(File cities, File roads) {
    this.cities = cities;
    this.roads = roads;
    this.nameCityMap = new HashMap<>();
    this.idCityMap = new HashMap<>();
    this.outputRoads = new HashMap<City, Set<Road>>();

    try {
      // Lire le fichier cities.txt
      Scanner citiesScanner = new Scanner(cities);
      while (citiesScanner.hasNextLine()) {
        String line = citiesScanner.nextLine();
        String[] cityData = line.split(",");
        int id = Integer.parseInt(cityData[0]);
        String name = cityData[1];
        double longitude = Double.parseDouble(cityData[2]);
        double latitude = Double.parseDouble(cityData[3]);
        City city = new City(id, name, longitude, latitude);
        nameCityMap.put(name, city);
        idCityMap.put(id, city);
        ajouterSommet(city);
      }
      citiesScanner.close();

      // Lire le fichier roads.txt
      Scanner roadsScanner = new Scanner(roads);
      while (roadsScanner.hasNextLine()) {
        String line = roadsScanner.nextLine();
        String[] roadData = line.split(",");
        City city1 = idCityMap.get(Integer.parseInt(roadData[0]));
        City city2 = idCityMap.get(Integer.parseInt(roadData[1]));
        Road road1 = new Road(city1, city2);
        Road road2 = new Road(city2, city1);
        ajouterArc(road1);
        ajouterArc(road2);
      }
      roadsScanner.close();
    } catch (FileNotFoundException e) {
      System.out.println("Erreur lors de la lecture des fichiers");
      e.printStackTrace();
    }
  }

  protected void ajouterSommet(City c) {
    if (outputRoads.containsKey(c)) {
      return;
    }
    Set<Road> ensembleArcs = new HashSet<>();
    outputRoads.put(c, ensembleArcs);
  }

  protected void ajouterArc(Road r) {
    City departureCity = r.getDepartureCity();
    outputRoads.get(departureCity).add(r);
  }

  public void calculerItineraireMinimisantNombreRoutes(String departureCity, String arrivalCity) {
    City startCity = nameCityMap.get(departureCity);
    City endCity = nameCityMap.get(arrivalCity);

    Queue<City> queue = new ArrayDeque<City>();
    queue.add(startCity);
    Map<City, Road> mapItineraire = new HashMap<City, Road>();
    mapItineraire.put(startCity, null);

    double distanceTotale = 0;
    int nbrRoutes = 0;

    if (startCity == null || endCity == null) {
      throw new IllegalArgumentException("Les villes sont invalides");
    }

    while (!queue.isEmpty()) {
      City currentCity = queue.poll();

      if (currentCity.equals(endCity)) {
        break;
      }

      for (Road road : outputRoads.get(currentCity)) {
        City nextCity = road.getArrivalCity();
        if (!mapItineraire.containsKey(nextCity)) {
          mapItineraire.put(nextCity, road);
          queue.add(nextCity);
        }
      }
    }

    List<Road> itineraire = new ArrayList<Road>();
    City city = endCity;
    while (mapItineraire.get(city) != null) {
      Road road = mapItineraire.get(city);
      itineraire.add(road);
      city = road.getDepartureCity();
      nbrRoutes++;
    }

    Collections.reverse(itineraire);

    for (Road road : itineraire) {
      double distance = Util.distance(road.getDepartureCity().getLongitude(),
          road.getDepartureCity().getLatitude(),
          road.getArrivalCity().getLongitude(), road.getArrivalCity().getLatitude());
      distanceTotale += distance;
      System.out.println(road + " (" + formatDecimal(distance) + " km)");

    }
    System.out.println(
        "Trajet de " + departureCity + " à " + arrivalCity + ": " + nbrRoutes + " route et "
            + distanceTotale + " kms");
  }

  // TODO - Dijkstra
  // Méthode pour calculer l'itinéraire minimisant les kilomètres en utilisant Dijkstra
  public void calculerItineraireMinimisantKm(String departureCity, String arrivalCity) {
    City startCity = nameCityMap.get(departureCity);
    City endCity = nameCityMap.get(arrivalCity);

    if (startCity == null || endCity == null) {
      throw new IllegalArgumentException("Les villes sont invalides");
    }

    // Initialiser les distances avec une valeur infinie, sauf pour la ville de départ
    Map<City, Double> distances = new HashMap<>();
    for (City city : nameCityMap.values()) {
      distances.put(city, Double.POSITIVE_INFINITY);
    }
    distances.put(startCity, 0.0);

    // Initialiser la file de priorité pour maintenir les villes à explorer
    Queue<City> priorityQueue = new PriorityQueue<>((c1, c2) -> Double.compare(distances.get(c1), distances.get(c2)));
    priorityQueue.add(startCity);

    // Map pour stocker les arêtes du chemin le plus court
    Map<City, Road> shortestPathEdges = new HashMap<>();

    while (!priorityQueue.isEmpty()) {
      City currentCity = priorityQueue.poll();

      if (currentCity.equals(endCity)) {
        break; // On a trouvé le chemin le plus court jusqu'à la ville d'arrivée
      }

      for (Road road : outputRoads.get(currentCity)) {
        City nextCity = road.getArrivalCity();
        double newDistance = distances.get(currentCity) + Util.distance(
            road.getDepartureCity().getLongitude(), road.getDepartureCity().getLatitude(),
            road.getArrivalCity().getLongitude(), road.getArrivalCity().getLatitude());

        if (newDistance < distances.get(nextCity)) {
          distances.put(nextCity, newDistance);
          shortestPathEdges.put(nextCity, road);
          priorityQueue.add(nextCity);
        }
      }
    }

    // Reconstituer l'itinéraire à partir de la ville d'arrivée
    List<Road> shortestPath = new ArrayList<>();
    City currentCity = endCity;
    while (shortestPathEdges.containsKey(currentCity)) {
      Road road = shortestPathEdges.get(currentCity);
      shortestPath.add(road);
      currentCity = road.getDepartureCity();
    }

    // Inverser la liste pour avoir l'itinéraire dans l'ordre
    Collections.reverse(shortestPath);

    // Afficher l'itinéraire
    double distanceTotale = 0;
    for (Road road : shortestPath) {
      double distance = Util.distance(road.getDepartureCity().getLongitude(),
          road.getDepartureCity().getLatitude(),
          road.getArrivalCity().getLongitude(), road.getArrivalCity().getLatitude());
      distanceTotale += distance;
      System.out.println(road + " (" + formatDecimal(distance) + " km)");
    }

    System.out.println(
        "Trajet de " + departureCity + " à " + arrivalCity + ": " + distanceTotale + " kms");
  }

  private static String formatDecimal(double number) {
    long rounded = Math.round(number * 100);
    double result = (double) rounded / 100;

    if (result == (long) result) {
      return String.format("%d", (long) result);
    } else {
      return String.format("%s", result);
    }
  }
}
