import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { accountantApi } from '../../api/accountant';
import {
  Card,
  Button,
  Input,
  LoadingSpinner,
  ErrorState,
  EmptyState,
} from '../../components/shared';
import { formatDate } from '../../utils/formatters';

export default function ClientsPage() {
  const [searchQuery, setSearchQuery] = useState('');

  const { data: clients, isLoading, error, refetch } = useQuery({
    queryKey: ['accountant-clients'],
    queryFn: accountantApi.getClients,
  });

  const filteredClients = clients?.filter(
    (client) =>
      client.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      client.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      client.lastName?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-brand-green">Clients</h1>

      <Card padding="sm">
        <Input
          placeholder="Search by name or email..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </Card>

      {isLoading && (
        <div className="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      )}

      {error && <ErrorState message="Failed to load clients" onRetry={refetch} />}

      {!isLoading && !error && (!filteredClients || filteredClients.length === 0) && (
        <EmptyState
          title="No clients found"
          description={searchQuery ? 'Try adjusting your search' : 'No clients registered yet'}
        />
      )}

      {!isLoading && !error && filteredClients && filteredClients.length > 0 && (
        <>
          <div className="hidden lg:block">
            <Card padding="none">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Client
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Email
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Phone
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Orders
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                      Joined
                    </th>
                    <th className="px-4 py-3"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredClients.map((client) => (
                    <tr key={client.id} className="hover:bg-gray-50">
                      <td className="px-4 py-4">
                        <p className="font-medium text-brand-green">
                          {client.firstName && client.lastName
                            ? `${client.firstName} ${client.lastName}`
                            : '-'}
                        </p>
                      </td>
                      <td className="px-4 py-4 text-gray-600">{client.email}</td>
                      <td className="px-4 py-4 text-gray-600">{client.phone || '-'}</td>
                      <td className="px-4 py-4 text-gray-600">{client.orderCount}</td>
                      <td className="px-4 py-4 text-gray-500">{formatDate(client.createdAt)}</td>
                      <td className="px-4 py-4 text-right">
                        <Link to={`/accountant/clients/${client.id}`}>
                          <Button variant="ghost" size="sm">
                            View
                          </Button>
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Card>
          </div>

          <div className="lg:hidden space-y-4">
            {filteredClients.map((client) => (
              <Link key={client.id} to={`/accountant/clients/${client.id}`}>
                <Card className="hover:shadow-md transition-shadow">
                  <div className="flex items-start justify-between">
                    <div>
                      <p className="font-semibold text-brand-green">
                        {client.firstName && client.lastName
                          ? `${client.firstName} ${client.lastName}`
                          : client.email}
                      </p>
                      {client.firstName && (
                        <p className="text-sm text-gray-500">{client.email}</p>
                      )}
                      <p className="text-sm text-gray-600 mt-1">
                        {client.orderCount} orders • Joined {formatDate(client.createdAt)}
                      </p>
                    </div>
                    <svg
                      className="w-5 h-5 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 5l7 7-7 7"
                      />
                    </svg>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
