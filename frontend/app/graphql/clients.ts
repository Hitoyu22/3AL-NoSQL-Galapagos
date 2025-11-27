import { gql } from 'nuxt-graphql-request/utils'
import type { Client } from './types'


const GET_CLIENTS = gql`
  query GetClients($name: String) {
    clients(name: $name) {
      id
      name
      type
      specialty
      study
      email
      orderHistory
    }
  }
`

const CREATE_CLIENT = gql`
  mutation CreateClient($name: String!, $type: String!, $specialty: String, $study: String, $email: String!) {
    createClient(name: $name, type: $type, specialty: $specialty, study: $study, email: $email) {
      id
      name
      email
    }
  }
`

const UPDATE_CLIENT = gql`
  mutation UpdateClient($id: ID!, $name: String, $type: String, $specialty: String, $study: String, $email: String) {
    updateClient(id: $id, name: $name, type: $type, specialty: $specialty, study: $study, email: $email) {
      id
      name
      specialty
    }
  }
`

const DELETE_CLIENT = gql`
  mutation DeleteClient($id: ID!) {
    deleteClient(id: $id)
  }
`


export const clientService = {

  async getAll(clientGql: any, nameFilter?: string): Promise<Client[]> {
    const variables = nameFilter ? { name: nameFilter } : {}
    const data = await clientGql.request(GET_CLIENTS, variables)
    return data.clients
  },


  async create(clientGql: any, client: Omit<Client, 'id' | 'orderHistory'>): Promise<Client> {
    const data = await clientGql.request(CREATE_CLIENT, {
      name: client.name,
      type: client.type,
      specialty: client.specialty,
      study: client.study,
      email: client.email
    })
    return data.createClient
  },


  async update(clientGql: any, client: Client): Promise<Client> {
    const data = await clientGql.request(UPDATE_CLIENT, {
      id: client.id,
      name: client.name,
      type: client.type,
      specialty: client.specialty,
      study: client.study,
      email: client.email
    })
    return data.updateClient
  },


  async delete(clientGql: any, id: string): Promise<boolean> {
    const data = await clientGql.request(DELETE_CLIENT, { id })
    return data.deleteClient
  }
}