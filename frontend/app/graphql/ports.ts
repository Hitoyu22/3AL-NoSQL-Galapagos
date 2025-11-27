import { gql } from 'nuxt-graphql-request/utils'
import type { Port, Locker } from './types'

const GET_PORTS = gql`
  query GetPorts($id: Int, $name: String, $islandName: String) {
    ports(id: $id, name: $name, islandName: $islandName) {
      id
      name
      islandName
      lat
      lon
      nbLockers
      lockers {
        id
        portId
        number
        status
        boxId
        maintenanceReason
        lastUsed
      }
    }
  }
`

export const portService = {
  async getAll(clientGql: any, filters: { id?: number, name?: string, islandName?: string } = {}): Promise<Port[]> {
    const variables = {
      id: filters.id,
      name: filters.name,
      islandName: filters.islandName
    }
    
    const data = await clientGql.request(GET_PORTS, variables)
    
    return data.ports.map((p: any) => ({
      ...p,
      island: p.islandName,
      lockers: p.nbLockers,
      lockersList: p.lockers as Locker[] 
    }))
  },

  async getByName(clientGql: any, name: string): Promise<Port | null> {
    const ports = await this.getAll(clientGql, { name })
    return ports[0] ?? null
  },

  async getById(clientGql: any, id: number): Promise<Port | null> {
    const ports = await this.getAll(clientGql, { id })
    return ports[0] ?? null
  }
}