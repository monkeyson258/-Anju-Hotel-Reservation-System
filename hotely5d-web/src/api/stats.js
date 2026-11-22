import request from '@/utils/request'

export const getOverviewAPI = () => request.get('/stats/overview')
export const getCheckInTrendAPI = (days = 7) => request.get('/stats/checkInTrend', { params: { days } })
export const getCategoryOccupancyAPI = () => request.get('/stats/categoryOccupancy')
export const getRevenueTrendAPI = (months = 6) => request.get('/stats/revenueTrend', { params: { months } })
export const getIntelligenceAPI = () => request.get('/stats/intelligence')
export const getPublicStatsAPI = () => request.get('/stats/public')
