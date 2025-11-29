export interface Port {
  id?: number;
  name: string;
  island: string;
  lat: number;
  lon: number;
  lockers: number;
  nbLockers?: number;
  lockersList?: Locker[];
  isWarehouse?: boolean;
}

export interface Seaplane {
  id: string;
  model: string;
  status: "AVAILABLE" | "MAINTENANCE" | "IN_FLIGHT" | "AT_PORT";
  location: string;
  lat: number;
  lon: number;
  route?: boolean;
  boxCapacity?: number;
  fuelConsumptionKm?: number;
  cruiseSpeedKmh?: number;
}

export interface Client {
  id: string;
  name: string;
  type: "chercheur" | "institution";
  specialty: string;
  study: string;
  email: string;
  orderHistory?: string[];
}

export interface Locker {
  id: string;
  portName: string;
  number: number;
  status: "EMPTY" | "OCCUPIED" | "RESERVED" | "MAINTENANCE";
  maintenanceReason?: string;
  boxId?: string;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  weightKg: number;
  stockAvailable?: number;
}

export interface OrderItem {
  product: Product;
  quantity: number;
}

export interface Order {
  id: string;
  clientId: string;
  clientName: string;
  date: string;
  status: "PENDING" | "IN_TRANSIT" | "DELIVERED" | "PARTIALLY_DELIVERED";
  destinationPort: string;
  items: OrderItem[];
  boxCount: number;
  totalWeight: number;
}
