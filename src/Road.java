public class Road {

  private City departureCity;
  private City arrivalCity;

  public Road(City departureCity, City arrivalCity) {
    this.departureCity = departureCity;
    this.arrivalCity = arrivalCity;
  }

  public City getDepartureCity() {
    return departureCity;
  }

  public void setDepartureCity(City departureCity) {
    this.departureCity = departureCity;
  }

  public City getArrivalCity() {
    return arrivalCity;
  }

  public void setArrivalCity(City arrivalCity) {
    this.arrivalCity = arrivalCity;
  }

  @Override
  public String toString() {
    return departureCity + " -> " + arrivalCity;
  }
}
