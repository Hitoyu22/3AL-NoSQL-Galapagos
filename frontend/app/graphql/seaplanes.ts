import { gql } from 'nuxt-graphql-request/utils'
import type { Seaplane } from './types'

const GET_SEAPLANES = gql`
  query GetSeaplanes($id: String) {
    seaplanes(id: $id) {
      id
      model
      status
      boxCapacity
      fuelConsumptionKm
      cruiseSpeedKmh
      currentLocation {
        portName
        lat
        lon
        island {
          id
          name
        }
      }
    }
  }
`

const CREATE_SEAPLANE = gql`
  mutation CreateSeaplane(
    $id: String!
    $model: String!
    $boxCapacity: Int!
    $fuelConsumptionKm: Float!
    $cruiseSpeedKmh: Float!
    $status: String!
  ) {
    createSeaplane(
      id: $id
      model: $model
      boxCapacity: $boxCapacity
      fuelConsumptionKm: $fuelConsumptionKm
      cruiseSpeedKmh: $cruiseSpeedKmh
      status: $status
    ) {
      id
    }
  }
`

const UPDATE_SEAPLANE = gql`
  mutation UpdateSeaplane(
    $id: String!
    $model: String
    $boxCapacity: Int
    $fuelConsumptionKm: Float
    $cruiseSpeedKmh: Float
    $status: String
  ) {
    updateSeaplane(
      id: $id
      model: $model
      boxCapacity: $boxCapacity
      fuelConsumptionKm: $fuelConsumptionKm
      cruiseSpeedKmh: $cruiseSpeedKmh
      status: $status
    ) {
      id
    }
  }
`

const mapGqlToSeaplane = (data: any): Seaplane => {
  return {
    id: data.id,
    model: data.model,
    status: data.status as any,
    boxCapacity: data.boxCapacity,
    fuelConsumptionKm: data.fuelConsumptionKm,
    cruiseSpeedKmh: data.cruiseSpeedKmh,
    location: data.currentLocation?.portName || 'Position inconnue',
    lat: data.currentLocation?.lat || 0,
    lon: data.currentLocation?.lon || 0,
    route: data.status === 'IN_FLIGHT'
  }
}

export const seaplaneService = {
  async getAll(clientGql: any, id?: string): Promise<Seaplane[]> {
    const variables = id ? { id } : {}
    const data = await clientGql.request(GET_SEAPLANES, variables)
    return data.seaplanes.map(mapGqlToSeaplane)
  },

  async create(clientGql: any, plane: Partial<Seaplane>): Promise<Seaplane> {
    const variables = {
      id: plane.id,
      model: plane.model,
      boxCapacity: Number(plane.boxCapacity),
      fuelConsumptionKm: Number(plane.fuelConsumptionKm),
      cruiseSpeedKmh: Number(plane.cruiseSpeedKmh),
      status: plane.status
    }
    const data = await clientGql.request(CREATE_SEAPLANE, variables)
    return data.createSeaplane
  },

  async update(clientGql: any, plane: Seaplane): Promise<Seaplane> {
    const variables = {
      id: plane.id,
      model: plane.model,
      boxCapacity: Number(plane.boxCapacity),
      fuelConsumptionKm: Number(plane.fuelConsumptionKm),
      cruiseSpeedKmh: Number(plane.cruiseSpeedKmh),
      status: plane.status
    }
    const data = await clientGql.request(UPDATE_SEAPLANE, variables)
    return data.updateSeaplane
  }
}