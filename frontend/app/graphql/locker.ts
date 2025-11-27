import { gql } from 'nuxt-graphql-request/utils'
import type { Locker } from './types'


const GET_LOCKERS = gql`
  query GetLockers($portId: Int, $status: String) {
    lockers(portId: $portId, status: $status) {
      id
      portId
      number
      status
      boxId
      maintenanceReason
      lastUsed
    }
  }
`


const ADD_LOCKER = gql`
  mutation AddLocker($portId: Int!) {
    addLocker(portId: $portId) {
      id
      portId
      number
      status
    }
  }
`

const UPDATE_LOCKER_STATUS = gql`
  mutation UpdateLockerStatus($id: String!, $status: String!, $maintenanceReason: String) {
    updateLockerStatus(id: $id, status: $status, maintenanceReason: $maintenanceReason) {
      id
      number
      status
      maintenanceReason
    }
  }
`

const DELETE_LOCKER = gql`
  mutation DeleteLocker($id: String!) {
    deleteLocker(id: $id)
  }
`

export const lockerService = {
  
  async getByPort(clientGql: any, portId: number): Promise<Locker[]> {
    const data = await clientGql.request(GET_LOCKERS, { portId })
    return data.lockers
  },

  async create(clientGql: any, portId: number): Promise<Locker> {
    const data = await clientGql.request(ADD_LOCKER, { portId })
    return data.addLocker
  },

  async updateStatus(clientGql: any, id: string, status: string, maintenanceReason?: string): Promise<Locker> {
    const variables = {
      id,
      status,
      maintenanceReason: status === 'MAINTENANCE' ? maintenanceReason : null
    }
    const data = await clientGql.request(UPDATE_LOCKER_STATUS, variables)
    return data.updateLockerStatus
  },

  async delete(clientGql: any, id: string): Promise<boolean> {
    const data = await clientGql.request(DELETE_LOCKER, { id })
    return data.deleteLocker
  }
}