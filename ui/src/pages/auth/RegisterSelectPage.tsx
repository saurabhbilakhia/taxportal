import { Link } from 'react-router-dom';
import { Card } from '../../components/shared';

export default function RegisterSelectPage() {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-8 bg-gray-50">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-brand-green">Create Account</h1>
          <p className="text-gray-600 mt-2">Choose your account type</p>
        </div>

        <div className="space-y-4">
          <Link to="/register/client" className="block">
            <Card className="cursor-pointer hover:border-brand-green hover:shadow-md transition-all">
              <div className="p-2">
                <h2 className="text-xl font-semibold text-gray-900">Client Account</h2>
                <p className="text-gray-600 mt-2">
                  For individuals needing tax services
                </p>
              </div>
            </Card>
          </Link>

          <Link to="/register/accountant" className="block">
            <Card className="cursor-pointer hover:border-brand-green hover:shadow-md transition-all">
              <div className="p-2">
                <h2 className="text-xl font-semibold text-gray-900">Accountant Account</h2>
                <p className="text-gray-600 mt-2">
                  For licensed tax professionals
                </p>
                <p className="text-sm text-amber-600 mt-1">
                  Requires admin approval
                </p>
              </div>
            </Card>
          </Link>
        </div>

        <div className="mt-6 text-center">
          <p className="text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="text-brand-green font-medium hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
